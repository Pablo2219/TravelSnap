package com.industriaop.travelsnap.util

import androidx.annotation.StringRes
import com.industriaop.travelsnap.domain.model.AppLanguage
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.Province
import com.industriaop.travelsnap.domain.model.SortOrder
import com.industriaop.travelsnap.R
/** Maps domain enums to localized labels. Use with stringResource(...) in Compose. */

@StringRes
fun PlaceCategory.labelRes(): Int = when (this) {
    PlaceCategory.BEACH -> R.string.category_beach
    PlaceCategory.MOUNTAIN -> R.string.category_mountain
    PlaceCategory.VOLCANO -> R.string.category_volcano
    PlaceCategory.WATERFALL -> R.string.category_waterfall
    PlaceCategory.NATURAL_PARK -> R.string.category_natural_park
    PlaceCategory.OTHER -> R.string.category_other
}

@StringRes
fun Province.labelRes(): Int = when (this) {
    Province.SAN_JOSE -> R.string.province_san_jose
    Province.ALAJUELA -> R.string.province_alajuela
    Province.CARTAGO -> R.string.province_cartago
    Province.HEREDIA -> R.string.province_heredia
    Province.GUANACASTE -> R.string.province_guanacaste
    Province.PUNTARENAS -> R.string.province_puntarenas
    Province.LIMON -> R.string.province_limon
}

@StringRes
fun SortOrder.labelRes(): Int = when (this) {
    SortOrder.NAME_ASC -> R.string.sort_name_asc
    SortOrder.NAME_DESC -> R.string.sort_name_desc
}

@StringRes
fun AppLanguage.labelRes(): Int = when (this) {
    AppLanguage.ENGLISH -> R.string.language_english
    AppLanguage.SPANISH -> R.string.language_spanish
}