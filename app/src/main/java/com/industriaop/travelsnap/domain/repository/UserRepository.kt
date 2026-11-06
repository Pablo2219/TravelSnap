package com.industriaop.travelsnap.domain.repository

import com.industriaop.travelsnap.domain.model.AppLanguage
import com.industriaop.travelsnap.domain.model.User
import com.industriaop.travelsnap.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeCurrentUser(): Flow<User?>
    fun observeSettings(): Flow<UserSettings>
    suspend fun login(email: String, password: String): User?
    suspend fun logout()
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setDarkTheme(enabled: Boolean)
}
