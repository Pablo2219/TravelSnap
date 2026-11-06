package com.industriaop.travelsnap.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.industriaop.travelsnap.domain.repository.PlaceRepository
import com.industriaop.travelsnap.domain.repository.UserRepository

/**
 * Factories for the ViewModels that are still used by login, profile and place form.
 * The main places list, detail and map screens now access the repository directly.
 */
object ViewModelFactories {

    fun placeForm(repository: PlaceRepository, placeId: Long?): ViewModelProvider.Factory =
        viewModelFactory { initializer { PlaceFormViewModel(repository, placeId) } }

    fun auth(repository: UserRepository): ViewModelProvider.Factory = viewModelFactory {
        initializer { AuthViewModel(repository) }
    }

    fun settings(repository: UserRepository): ViewModelProvider.Factory = viewModelFactory {
        initializer { SettingsViewModel(repository) }
    }
}
