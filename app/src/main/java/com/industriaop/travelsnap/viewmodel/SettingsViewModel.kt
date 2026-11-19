package com.industriaop.travelsnap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.domain.model.AppLanguage

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as TravelSnapApp
    private val repository = app.repository

    fun getCurrentUser() = repository.getCurrentUser()

    fun getSettings() = repository.getSettings()

    fun setLanguage(language: AppLanguage) {
        repository.setLanguage(language)
        app.applySettings()
    }

    fun setDarkTheme(enabled: Boolean) {
        repository.setDarkTheme(enabled)
        app.applySettings()
    }

    fun logout() = repository.logout()
}
