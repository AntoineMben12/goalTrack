package com.example.goaltrack.data.repository

import com.example.goaltrack.data.model.UserProfile
import com.example.goaltrack.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase-backed implementation of [AuthRepository].
 *
 * Uses Supabase GoTrue for auth operations and Postgrest to read/write the
 * `profiles` table (which is auto-created on first sign-up via a DB trigger).
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<UserProfile> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = getCurrentUser() ?: return Result.failure(Exception("User not found after sign-in"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        username: String,
        fullName: String
    ): Result<UserProfile> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = kotlinx.serialization.json.buildJsonObject {
                    put("username", kotlinx.serialization.json.JsonPrimitive(username))
                    put("full_name", kotlinx.serialization.json.JsonPrimitive(fullName))
                }
            }
            val user = getCurrentUser() ?: return Result.failure(Exception("Profile creation pending"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            supabase.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): UserProfile? {
        val session = supabase.auth.currentSessionOrNull() ?: return null
        val userId = session.user?.id ?: return null
        return try {
            supabase.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<UserProfile>()
        } catch (e: Exception) {
            // Return a minimal profile from session data if table query fails
            UserProfile(
                id = userId,
                username = session.user?.email?.substringBefore("@") ?: "",
                fullName = ""
            )
        }
    }

    override fun observeAuthState(): Flow<UserProfile?> {
        return supabase.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> getCurrentUser()
                else -> null
            }
        }
    }
}
