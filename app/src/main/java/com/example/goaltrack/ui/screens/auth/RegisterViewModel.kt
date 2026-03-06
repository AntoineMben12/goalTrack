package com.example.goaltrack.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goaltrack.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    // Inline field errors
    val fullNameError: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChange(v: String) = _uiState.update { it.copy(fullName = v, fullNameError = null) }
    fun onUsernameChange(v: String) = _uiState.update { it.copy(username = v, usernameError = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, emailError = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, passwordError = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, confirmPasswordError = null) }
    fun onTogglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    fun onToggleConfirmVisibility() = _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun validate(): Boolean {
        val s = _uiState.value
        var valid = true
        if (s.fullName.isBlank()) {
            _uiState.update { it.copy(fullNameError = "Full name is required") }; valid = false
        }
        if (s.username.isBlank() || s.username.length < 3) {
            _uiState.update { it.copy(usernameError = "Username must be at least 3 characters") }; valid = false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches()) {
            _uiState.update { it.copy(emailError = "Invalid email address") }; valid = false
        }
        if (s.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }; valid = false
        }
        if (s.password != s.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }; valid = false
        }
        return valid
    }

    fun onRegisterClick() {
        if (!validate()) return
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.signUp(
                email = s.email.trim(),
                password = s.password,
                username = s.username.trim(),
                fullName = s.fullName.trim()
            )
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSuccess = true) } },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Registration failed")
                    }
                }
            )
        }
    }
}
