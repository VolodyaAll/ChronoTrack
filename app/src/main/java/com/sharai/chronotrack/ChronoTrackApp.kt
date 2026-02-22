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
import android.util.Log
import androidx.core.os.LocaleListCompat

class ChronoTrackApp : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    
    val activityRepository by lazy { ActivityRepository(database.activityDao()) }
    val timeEntryRepository by lazy { TimeEntryRepository(database.timeEntryDao()) }
    val commentRepository by lazy { CommentRepository(database.commentDao()) }
    
    // Добавляем доступ к настройкам приложения
    val appPreferences by lazy { AppPreferences(this) }
    
    // Создаем корутин-скоуп для приложения
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Инициализируем язык приложения при запуске
        applicationScope.launch {
            val languageCode = appPreferences.language.first()
            Log.d("ChronoTrackApp", "Инициализация с языком: $languageCode")
            updateLocale(languageCode)
        }
    }
    
    // Метод для обновления локали приложения
    fun updateLocale(languageCode: String) {
        Log.d("ChronoTrackApp", "Обновление языка на: $languageCode")
        
        try {
            if (languageCode == AppPreferences.SYSTEM_LANGUAGE) {
                // Используем системный язык
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                Log.d("ChronoTrackApp", "Установлен системный язык")
            } else {
                // Устанавливаем выбранный язык
                val locale = appPreferences.getLocale(languageCode)
                
                // Устанавливаем локаль по умолчанию
                Locale.setDefault(locale)
                
                // Создаем список локалей с выбранной локалью
                val localeList = LocaleListCompat.create(locale)
                
                // Устанавливаем локаль для всего приложения через AppCompatDelegate
                AppCompatDelegate.setApplicationLocales(localeList)
                
                Log.d("ChronoTrackApp", "Установлен язык: $languageCode, локаль: ${locale.language}")
            }
        } catch (e: Exception) {
            Log.e("ChronoTrackApp", "Ошибка при обновлении локали: ${e.message}", e)
        }
    }
    
    // Метод для получения текущего контекста с правильной локалью
    override fun attachBaseContext(base: Context) {
        // Получаем сохраненный язык синхронно через SharedPreferences
        val sharedPrefs = base.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("language", AppPreferences.SYSTEM_LANGUAGE) ?: AppPreferences.SYSTEM_LANGUAGE
        
        Log.d("ChronoTrackApp", "attachBaseContext с языком: $languageCode")
        
        // Если используется системный язык, используем базовый контекст
        if (languageCode == AppPreferences.SYSTEM_LANGUAGE) {
            super.attachBaseContext(base)
            return
        }
        
        // Создаем локаль на основе сохраненного языка
        val locale = when (languageCode) {
            "ru" -> Locale("ru")
            "en" -> Locale("en")
            else -> Locale.getDefault()
        }
        
        // Создаем конфигурацию с нужной локалью
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        
        // Создаем контекст с обновленной конфигурацией
        val updatedContext = base.createConfigurationContext(config)
        super.attachBaseContext(updatedContext)
    }
} 