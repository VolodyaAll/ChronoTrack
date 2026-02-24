package com.sharai.chronotrack

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.sharai.chronotrack.data.database.AppDatabase
import com.sharai.chronotrack.data.preferences.AppPreferences
import com.sharai.chronotrack.repository.ActivityRepository
import com.sharai.chronotrack.repository.CommentRepository
import com.sharai.chronotrack.repository.TimeEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.os.LocaleListCompat

class ChronoTrackApp : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    val activityRepository by lazy { ActivityRepository(database.activityDao()) }
    val timeEntryRepository by lazy { TimeEntryRepository(database.timeEntryDao()) }
    val commentRepository by lazy { CommentRepository(database.commentDao()) }
    val appPreferences by lazy { AppPreferences(this) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            val languageCode = appPreferences.language.first()
            updateLocale(languageCode)
        }
    }

    fun updateLocale(languageCode: String) {
        try {
            if (languageCode == AppPreferences.SYSTEM_LANGUAGE) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            } else {
                val locale = appPreferences.getLocale(languageCode)
                Locale.setDefault(locale)
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
            }
        } catch (_: Exception) {
        }
    }

    override fun attachBaseContext(base: Context) {
        val sharedPrefs = base.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("language", AppPreferences.SYSTEM_LANGUAGE)
            ?: AppPreferences.SYSTEM_LANGUAGE

        if (languageCode == AppPreferences.SYSTEM_LANGUAGE) {
            super.attachBaseContext(base)
            return
        }

        val locale = when (languageCode) {
            "ru" -> Locale("ru")
            "en" -> Locale("en")
            else -> Locale.getDefault()
        }

        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(base.createConfigurationContext(config))
    }
}
