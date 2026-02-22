package com.sharai.chronotrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sharai.chronotrack.data.preferences.AppPreferences
import com.sharai.chronotrack.data.preferences.Language
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val appPreferences: AppPreferences) : ViewModel() {
    
    // Текущий выбранный язык
    val currentLanguage: StateFlow<String> = appPreferences.language
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppPreferences.SYSTEM_LANGUAGE
        )
    
    // Список поддерживаемых языков
    val supportedLanguages: List<Language> = AppPreferences.SUPPORTED_LANGUAGES
    
    // Метод для изменения языка
    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            appPreferences.setLanguage(languageCode)
        }
    }
    
    // Factory для создания ViewModel с зависимостями
    class Factory(private val appPreferences: AppPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(appPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 