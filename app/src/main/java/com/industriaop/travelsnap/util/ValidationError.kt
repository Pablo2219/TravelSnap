package com.industriaop.travelsnap.util

import androidx.annotation.StringRes
import com.industriaop.travelsnap.R
enum class ValidationError(@param:StringRes val messageRes: Int) {
    NAME_REQUIRED(R.string.error_name_required),
    NAME_TOO_SHORT(R.string.error_name_too_short),
    NAME_TOO_LONG(R.string.error_name_too_long),
    DESCRIPTION_REQUIRED(R.string.error_description_required),
    DESCRIPTION_TOO_LONG(R.string.error_description_too_long),
    CATEGORY_REQUIRED(R.string.error_category_required),
    PROVINCE_REQUIRED(R.string.error_province_required),
    LATITUDE_INVALID(R.string.error_latitude_invalid),
    LONGITUDE_INVALID(R.string.error_longitude_invalid),
    EMAIL_REQUIRED(R.string.error_email_required),
    EMAIL_INVALID(R.string.error_email_invalid),
    PASSWORD_REQUIRED(R.string.error_password_required),
    PASSWORD_TOO_SHORT(R.string.error_password_too_short)
}