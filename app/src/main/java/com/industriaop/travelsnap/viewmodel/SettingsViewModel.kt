package com.industriaop.travelsnap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.industriaop.travelsnap.domain.model.AppLanguage
import com.industriaop.travelsnap.domain.model.User
import com.industriaop.travelsnap.domain.model.UserSettings
import com.industriaop.travelsnap.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val user: User? = null,
    val settings: UserSettings = UserSettings()
)

/** Backs the "Profile and settings" screen. */
class SettingsViewModel(
    private val repository: UserRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.observeCurrentUser(), repository.observeSettings()
    ) { user, settings ->
        SettingsUiState(user, settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun onDarkThemeChange(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkTheme(enabled) }
    }

    fun onLogout() {
        viewModelScope.launch { repository.logout() }
    }
}