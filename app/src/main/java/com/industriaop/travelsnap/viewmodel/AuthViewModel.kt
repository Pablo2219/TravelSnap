package com.industriaop.travelsnap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.domain.repository.UserRepository

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository = (application as TravelSnapApp).repository

    fun login(email: String, password: String) = repository.login(email, password)
}
