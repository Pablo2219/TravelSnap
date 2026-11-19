package com.industriaop.travelsnap

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.industriaop.travelsnap.data.memory.MemoryDataRepository
import com.industriaop.travelsnap.domain.model.AppLanguage
import java.util.Locale

/** Shares the in-memory repository between all activities. */
class TravelSnapApp : Application() {

    val repository: MemoryDataRepository by lazy { MemoryDataRepository() }

    override fun onCreate() {
        super.onCreate()
        val systemIsDark =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        repository.setLanguage(AppLanguage.fromTag(Locale.getDefault().language))
        repository.setDarkTheme(systemIsDark)
        applySettings()
    }

    fun applySettings() {
        val settings = repository.getSettings()
        val nightMode = if (settings.darkTheme) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(settings.language.tag)
        )
    }
}