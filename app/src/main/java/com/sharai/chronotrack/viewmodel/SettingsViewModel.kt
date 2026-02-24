package com.sharai.chronotrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sharai.chronotrack.data.preferences.AppPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val appPreferences: AppPreferences) : ViewModel() {

    val currentLanguage: StateFlow<String> = appPreferences.language
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppPreferences.SYSTEM_LANGUAGE
        )

    val supportedLanguages = AppPreferences.SUPPORTED_LANGUAGES

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            appPreferences.setLanguage(languageCode)
        }
    }

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
