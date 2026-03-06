package com.example.goaltrack.domain.repository

import com.example.goaltrack.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Authentication repository interface — abstracts Supabase GoTrue operations.
 */
interface AuthRepository {

    /**
     * Sign in with email and password.
     * @return [Result.success] with [UserProfile] on success, [Result.failure] on error.
     */
    suspend fun signIn(email: String, password: String): Result<UserProfile>

    /**
     * Register a new user with email, password, and a display username.
     * @return [Result.success] with [UserProfile] on success.
     */
    suspend fun signUp(email: String, password: String, username: String, fullName: String): Result<UserProfile>

    /** Sign the current user out. */
    suspend fun signOut(): Result<Unit>

    /**
     * Returns the currently authenticated user's profile, or null if not signed in.
     */
    suspend fun getCurrentUser(): UserProfile?

    /**
     * Emits the signed-in user's profile whenever auth state changes.
     * Emits null when the user is signed out.
     */
    fun observeAuthState(): Flow<UserProfile?>
}
