package com.example.goaltrack.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goaltrack.data.model.AppTheme
import com.example.goaltrack.data.model.UserProfile
import com.example.goaltrack.data.model.UserSettings
import com.example.goaltrack.domain.repository.AuthRepository
import com.example.goaltrack.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val profile: UserProfile? = null,
    val settings: UserSettings = UserSettings(),
    val isLoading: Boolean = true,
    val isSigning: Boolean = false,
    val isSignedOut: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = authRepository.getCurrentUser()
            _uiState.update { it.copy(profile = profile) }
        }
        viewModelScope.launch {
            settingsRepository.getUserSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings, isLoading = false) }
            }
        }
    }

    fun onThemeChange(theme: AppTheme) = saveSettings { it.copy(theme = theme.name) }
    fun onNotificationsToggle(enabled: Boolean) = saveSettings { it.copy(notificationsEnabled = enabled) }
    fun onWeeklyReportToggle(enabled: Boolean) = saveSettings { it.copy(weeklyReportEnabled = enabled) }

    private fun saveSettings(update: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            val updated = update(_uiState.value.settings)
            _uiState.update { it.copy(settings = updated) }
            settingsRepository.updateSettings(updated)
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigning = true) }
            authRepository.signOut()
            _uiState.update { it.copy(isSigning = false, isSignedOut = true) }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
