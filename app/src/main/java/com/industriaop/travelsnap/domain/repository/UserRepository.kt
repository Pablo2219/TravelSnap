package com.industriaop.travelsnap.domain.repository

import com.industriaop.travelsnap.domain.model.AppLanguage
import com.industriaop.travelsnap.domain.model.UserSettings
import com.industriaop.travelsnap.domain.model.User
import kotlinx.coroutines.flow.Flow


interface UserRepository {

    fun observeCurrentUser(): Flow<User?>

    fun observeSettings(): Flow<UserSettings>

    /** Returns the authenticated user, or null when the credentials are wrong. */
    suspend fun login(email: String, password: String): User?

    suspend fun logout()

    suspend fun setLanguage(language: AppLanguage)

    suspend fun setDarkTheme(enabled: Boolean)
}