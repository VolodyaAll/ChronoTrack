package com.sharai.chronotrack.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferences(private val context: Context) {

    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("language")
        const val PREFS_NAME = "app_preferences"
        const val LANGUAGE_PREF_KEY = "language"

        val SUPPORTED_LANGUAGES = listOf(
            Language("en", "English"),
            Language("ru", "Русский")
        )

        const val SYSTEM_LANGUAGE = "system"
    }

    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: SYSTEM_LANGUAGE
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
        saveLanguageToSharedPrefs(languageCode)
    }

    /** Sync backup for use before DataStore is ready (e.g. attachBaseContext). */
    private fun saveLanguageToSharedPrefs(languageCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_PREF_KEY, languageCode)
            .apply()
    }

    fun getLanguageSync(): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_PREF_KEY, SYSTEM_LANGUAGE) ?: SYSTEM_LANGUAGE
    }

    fun getLocale(languageCode: String): Locale = when (languageCode) {
        "ru" -> Locale("ru")
        "en" -> Locale("en")
        else -> Locale.getDefault()
    }
}

data class Language(val code: String, val displayName: String)
