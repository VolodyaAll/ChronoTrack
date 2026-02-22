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
import android.util.Log

// Создаем экземпляр DataStore на уровне приложения
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferences(private val context: Context) {
    
    // Ключи для хранения настроек
    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("language")
        
        // Имя для SharedPreferences
        const val PREFS_NAME = "app_preferences"
        
        // Ключ для SharedPreferences
        const val LANGUAGE_PREF_KEY = "language"
        
        // Поддерживаемые языки
        val SUPPORTED_LANGUAGES = listOf(
            Language("en", "English"),
            Language("ru", "Русский")
        )
        
        // Язык по умолчанию (системный)
        const val SYSTEM_LANGUAGE = "system"
    }
    
    // Получение текущего языка через DataStore (асинхронно)
    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: SYSTEM_LANGUAGE
    }
    
    // Установка языка через DataStore (асинхронно)
    suspend fun setLanguage(languageCode: String) {
        Log.d("AppPreferences", "Сохранение языка в DataStore: $languageCode")
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
        
        // Дублируем сохранение в SharedPreferences для синхронного доступа
        saveLanguageToSharedPrefs(languageCode)
    }
    
    // Сохранение языка в SharedPreferences (синхронно)
    private fun saveLanguageToSharedPrefs(languageCode: String) {
        Log.d("AppPreferences", "Сохранение языка в SharedPreferences: $languageCode")
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(LANGUAGE_PREF_KEY, languageCode).apply()
    }
    
    // Получение языка из SharedPreferences (синхронно)
    fun getLanguageSync(): String {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val language = sharedPrefs.getString(LANGUAGE_PREF_KEY, SYSTEM_LANGUAGE) ?: SYSTEM_LANGUAGE
        Log.d("AppPreferences", "Получение языка из SharedPreferences: $language")
        return language
    }
    
    // Получение текущей локали на основе сохраненного языка
    fun getLocale(languageCode: String): Locale {
        return when (languageCode) {
            "ru" -> Locale("ru")
            "en" -> Locale("en")
            else -> Locale.getDefault() // Системный язык
        }
    }
}

// Класс для представления языка в UI
data class Language(val code: String, val displayName: String) 