package com.industriaop.travelsnap.domain.model

data class User(
    val id: Long,
    val name: String,
    val email: String
)

enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    SPANISH("es");

    companion object {
        fun fromTag(tag: String): AppLanguage =
            entries.firstOrNull { it.tag == tag } ?: ENGLISH
    }
}

data class UserSettings(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val darkTheme: Boolean = false
)