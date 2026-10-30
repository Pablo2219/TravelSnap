package com.industriaop.travelsnap

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.industriaop.travelsnap.data.memory.MemoryDataRepository
import com.industriaop.travelsnap.domain.model.AppLanguage
import com.industriaop.travelsnap.domain.model.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Application class. It owns the single [MemoryDataRepository] shared by all screens
 * and applies the language / dark theme chosen in the profile screen.
 */
class TravelSnapApp : Application() {

    /** One repository for the whole app, so every Activity sees the same data. */
    val repository: MemoryDataRepository by lazy { MemoryDataRepository() }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            // Start with the language and theme of the device, then follow the user's choice.
            val systemIsDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
            repository.setLanguage(AppLanguage.fromTag(Locale.getDefault().language))
            repository.setDarkTheme(systemIsDark)
            repository.observeSettings().collect { applySettings(it) }
        }
    }

    private fun applySettings(settings: UserSettings) {
        val nightMode = if (settings.darkTheme) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }

        val currentLanguage = AppCompatDelegate.getApplicationLocales()[0]?.language
            ?: Locale.getDefault().language
        if (currentLanguage != settings.language.tag) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(settings.language.tag)
            )
        }
    }
}
