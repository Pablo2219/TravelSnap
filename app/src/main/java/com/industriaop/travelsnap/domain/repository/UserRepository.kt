package com.industriaop.travelsnap.domain.repository

import com.industriaop.travelsnap.domain.model.AppLanguage
import com.industriaop.travelsnap.domain.model.User
import com.industriaop.travelsnap.domain.model.UserSettings

/** Basic user and settings operations. */
interface UserRepository {
    fun getCurrentUser(): User?
    fun getSettings(): UserSettings
    fun login(email: String, password: String): User?
    fun logout()
    fun setLanguage(language: AppLanguage)
    fun setDarkTheme(enabled: Boolean)
}
