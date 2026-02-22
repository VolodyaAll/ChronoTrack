package com.sharai.chronotrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sharai.chronotrack.data.model.TimeEntry
import com.sharai.chronotrack.repository.TimeEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

class TimeEntryViewModel(private val repository: TimeEntryRepository) : ViewModel() {
    private val _selectedTimeEntry = MutableStateFlow<TimeEntry?>(null)
    val selectedTimeEntry: StateFlow<TimeEntry?> = _selectedTimeEntry.asStateFlow()

    val allTimeEntries: Flow<List<TimeEntry>> = repository.getAllTimeEntries()

    fun getTimeEntriesForActivity(activityId: Long): Flow<List<TimeEntry>> =
        repository.getTimeEntriesForActivity(activityId)

    fun getTimeEntriesForPeriod(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<TimeEntry>> =
        repository.getTimeEntriesForPeriod(startDate, endDate)

    /**
     * Получает текущую запись времени
     * @return Текущая запись времени или null, если нет активной записи
     */
    suspend fun getCurrentTimeEntry(): TimeEntry? = repository.getCurrentTimeEntry()

    fun startActivity(activityId: Long) {
        viewModelScope.launch {
            // Завершаем текущую активность, если есть
            getCurrentTimeEntry()?.let { currentEntry ->
                val now = LocalDateTime.now()
                val duration = Duration.between(currentEntry.startTime, now)
                updateTimeEntry(currentEntry.copy(
                    endTime = now,
                    duration = duration
                ))
            }

            // Создаем новую запись
            val timeEntry = TimeEntry(
                activityId = activityId,
                startTime = LocalDateTime.now()
            )
            repository.insertTimeEntry(timeEntry)
        }
    }

    fun stopActivity() {
        viewModelScope.launch {
            getCurrentTimeEntry()?.let { currentEntry ->
                val now = LocalDateTime.now()
                val duration = Duration.between(currentEntry.startTime, now)
                updateTimeEntry(currentEntry.copy(
                    endTime = now,
                    duration = duration
                ))
            }
        }
    }

    private suspend fun updateTimeEntry(timeEntry: TimeEntry) {
        repository.updateTimeEntry(timeEntry)
    }

    /**
     * Удаляет запись времени по ID
     * @param timeEntryId ID записи времени для удаления
     */
    fun deleteTimeEntry(timeEntry: TimeEntry) {
        viewModelScope.launch {
            repository.deleteTimeEntry(timeEntry)
        }
    }

    class Factory(private val repository: TimeEntryRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TimeEntryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TimeEntryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 