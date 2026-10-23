package com.industriaop.travelsnap.util

import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.Province

object Validators {

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateName(name: String): ValidationError? {
        val value = name.trim()
        return when {
            value.isEmpty() -> ValidationError.NAME_REQUIRED
            value.length < Constants.MIN_NAME_LENGTH -> ValidationError.NAME_TOO_SHORT
            value.length > Constants.MAX_NAME_LENGTH -> ValidationError.NAME_TOO_LONG
            else -> null
        }
    }

    fun validateDescription(description: String): ValidationError? {
        val value = description.trim()
        return when {
            value.isEmpty() -> ValidationError.DESCRIPTION_REQUIRED
            value.length > Constants.MAX_DESCRIPTION_LENGTH -> ValidationError.DESCRIPTION_TOO_LONG
            else -> null
        }
    }

    fun validateCategory(category: PlaceCategory?): ValidationError? =
        if (category == null) ValidationError.CATEGORY_REQUIRED else null

    fun validateProvince(province: Province?): ValidationError? =
        if (province == null) ValidationError.PROVINCE_REQUIRED else null

    fun validateLatitude(text: String): ValidationError? {
        val value = text.trim().toDoubleOrNull()
        return if (value == null || value !in -90.0..90.0) ValidationError.LATITUDE_INVALID else null
    }

    fun validateLongitude(text: String): ValidationError? {
        val value = text.trim().toDoubleOrNull()
        return if (value == null || value !in -180.0..180.0) ValidationError.LONGITUDE_INVALID else null
    }

    fun validateEmail(email: String): ValidationError? {
        val value = email.trim()
        return when {
            value.isEmpty() -> ValidationError.EMAIL_REQUIRED
            !emailRegex.matches(value) -> ValidationError.EMAIL_INVALID
            else -> null
        }
    }

    fun validatePassword(password: String): ValidationError? = when {
        password.isEmpty() -> ValidationError.PASSWORD_REQUIRED
        password.length < Constants.MIN_PASSWORD_LENGTH -> ValidationError.PASSWORD_TOO_SHORT
        else -> null
    }
}