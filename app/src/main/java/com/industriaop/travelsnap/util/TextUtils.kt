package com.industriaop.travelsnap.util

import java.text.Normalizer

object TextUtils {

    private val diacritics = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    /** Lowercases, trims and removes accents so "Limón" matches "limon". */
    fun normalize(text: String): String =
        Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
            .replace(diacritics, "")
            .lowercase()
}