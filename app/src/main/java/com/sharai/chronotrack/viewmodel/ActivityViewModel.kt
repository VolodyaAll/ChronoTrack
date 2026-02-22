package com.sharai.chronotrack.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sharai.chronotrack.data.model.Activity
import com.sharai.chronotrack.repository.ActivityRepository
import com.sharai.chronotrack.repository.TimeEntryRepository
import com.sharai.chronotrack.data.model.TimeEntry
import com.sharai.chronotrack.service.TimeTrackingService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.Duration

class ActivityViewModel(
    private val repository: ActivityRepository,
    private val timeEntryRepository: TimeEntryRepository
) : ViewModel() {
    val allActiveActivities: Flow<List<Activity>> = repository.allActiveActivities
    val archivedActivities: Flow<List<Activity>> = repository.archivedActivities
    val currentActivity: Flow<Activity?> = repository.currentActivity

    private val _selectedActivity = MutableStateFlow<Activity?>(null)
    val selectedActivity: StateFlow<Activity?> = _selectedActivity

    private val _currentActivityStartTime = MutableStateFlow<LocalDateTime?>(null)
    val currentActivityStartTime: StateFlow<LocalDateTime?> = _currentActivityStartTime.asStateFlow()
    
    private val _selectedTimeEntry = MutableStateFlow<TimeEntry?>(null)
    val selectedTimeEntry: StateFlow<TimeEntry?> = _selectedTimeEntry.asStateFlow()
    
    private val _activityForSelectedTimeEntry = MutableStateFlow<Activity?>(null)
    val activityForSelectedTimeEntry: StateFlow<Activity?> = _activityForSelectedTimeEntry.asStateFlow()

    init {
        viewModelScope.launch {
            // Инициализируем время начала текущей активности
            timeEntryRepository.getCurrentTimeEntry()?.let { currentEntry ->
                _currentActivityStartTime.value = currentEntry.startTime
            }
            
            // Следим за изменениями текущей активности
            timeEntryRepository.getAllTimeEntries().collect { entries ->
                entries.find { it.endTime == null }?.let { currentEntry ->
                    _currentActivityStartTime.value = currentEntry.startTime
                }
            }
        }
    }

    fun clearSelectedActivity() {
        _selectedActivity.value = null
    }

    fun loadActivity(id: Long) {
        viewModelScope.launch {
            _selectedActivity.value = repository.getActivityById(id)
        }
    }
    
    fun loadTimeEntry(id: Long) {
        viewModelScope.launch {
            val timeEntry = timeEntryRepository.getTimeEntryById(id)
            _selectedTimeEntry.value = timeEntry
            
            if (timeEntry != null) {
                _activityForSelectedTimeEntry.value = repository.getActivityById(timeEntry.activityId)
            }
        }
    }

    fun insertActivity(activity: Activity) = viewModelScope.launch {
        repository.insertActivity(activity)
    }

    fun updateActivity(activity: Activity) = viewModelScope.launch {
        repository.updateActivity(activity)
    }

    fun archiveActivity(activityId: Long) = viewModelScope.launch {
        repository.archiveActivity(activityId)
    }

    fun restoreActivity(activityId: Long) = viewModelScope.launch {
        repository.restoreActivity(activityId)
    }
    
    fun deleteActivityWithTimeEntries(activityId: Long) = viewModelScope.launch {
        // Сначала удаляем все записи времени для этой активности
        timeEntryRepository.deleteTimeEntriesForActivity(activityId)
        
        // Затем удаляем саму активность
        repository.deleteActivity(activityId)
    }

    fun setCurrentActivity(activityId: Long, startTime: LocalDateTime) {
        viewModelScope.launch {
            val selectedActivity = repository.getActivityById(activityId)
            val currentAct = repository.currentActivity.first()
            
            // Не меняем активность, если выбрана текущая
            if (selectedActivity != null && currentAct?.id != activityId) {
                // Обновляем активность в базе данных
                repository.setCurrentActivity(activityId)
                
                // Вызываем метод смены активности с указанным временем в сервисе
                TimeTrackingService.getInstance()?.changeActivity(selectedActivity, startTime)
                
                // Обновляем время начала активности
                _currentActivityStartTime.value = startTime
            }
        }
    }

    class Factory(
        private val repository: ActivityRepository,
        private val timeEntryRepository: TimeEntryRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ActivityViewModel(repository, timeEntryRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 