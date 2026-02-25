package com.sharai.chronotrack

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.sharai.chronotrack.data.database.AppDatabase
import com.sharai.chronotrack.data.preferences.AppPreferences
import com.sharai.chronotrack.repository.ActivityRepository
import com.sharai.chronotrack.repository.CommentRepository
import com.sharai.chronotrack.repository.TimeEntryRepository
import java.util.Locale

class ChronoTrackApp : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    val activityRepository by lazy { ActivityRepository(database.activityDao()) }
    val timeEntryRepository by lazy { TimeEntryRepository(database.timeEntryDao()) }
    val commentRepository by lazy { CommentRepository(database.commentDao()) }
    val appPreferences by lazy { AppPreferences(this) }

    override fun attachBaseContext(base: Context) {
        val sharedPrefs = base.getSharedPreferences(AppPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString(AppPreferences.LANGUAGE_PREF_KEY, AppPreferences.SYSTEM_LANGUAGE)
            ?: AppPreferences.SYSTEM_LANGUAGE

        if (languageCode == AppPreferences.SYSTEM_LANGUAGE) {
            super.attachBaseContext(base)
            return
        }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(base.createConfigurationContext(config))
    }
}
