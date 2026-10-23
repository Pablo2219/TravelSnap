package com.industriaop.travelsnap.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.industriaop.travelsnap.domain.repository.PlaceRepository
import com.industriaop.travelsnap.domain.repository.UserRepository

object ViewModelFactories {

    fun placeList(repository: PlaceRepository): ViewModelProvider.Factory = viewModelFactory {
        initializer { PlaceListViewModel(repository) }
    }

    fun placeForm(repository: PlaceRepository, placeId: Long?): ViewModelProvider.Factory =
        viewModelFactory { initializer { PlaceFormViewModel(repository, placeId) } }

    fun placeDetail(repository: PlaceRepository, placeId: Long): ViewModelProvider.Factory =
        viewModelFactory { initializer { PlaceDetailViewModel(repository, placeId) } }

    fun map(repository: PlaceRepository): ViewModelProvider.Factory = viewModelFactory {
        initializer { MapViewModel(repository) }
    }

    fun auth(repository: UserRepository): ViewModelProvider.Factory = viewModelFactory {
        initializer { AuthViewModel(repository) }
    }

    fun settings(repository: UserRepository): ViewModelProvider.Factory = viewModelFactory {
        initializer { SettingsViewModel(repository) }
    }
}