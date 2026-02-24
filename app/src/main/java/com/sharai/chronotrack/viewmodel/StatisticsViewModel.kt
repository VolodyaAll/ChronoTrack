package com.sharai.chronotrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sharai.chronotrack.data.model.Activity
import com.sharai.chronotrack.data.model.TimeEntry
import com.sharai.chronotrack.repository.ActivityRepository
import com.sharai.chronotrack.repository.TimeEntryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Duration

data class TimeEntryWithActivity(
    val timeEntry: TimeEntry,
    val activity: Activity
)

data class DayStatistics(
    val date: LocalDate,
    val entries: List<TimeEntryWithActivity>
)

data class DateTimeRange(
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(0, 0),
    val endTime: LocalTime = LocalTime.of(23, 59, 59)
)

data class ActivityStatistics(
    val activity: Activity,
    val totalDuration: Duration,
    val entriesCount: Int,
    val percentage: Float
)

class StatisticsViewModel(
    private val timeEntryRepository: TimeEntryRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(DateTimeRange())
    val selectedRange: StateFlow<DateTimeRange> = _selectedRange.asStateFlow()

    val firstActivityDate: StateFlow<LocalDate> = timeEntryRepository.getFirstActivityDate()
        .stateIn(viewModelScope, SharingStarted.Eagerly, LocalDate.now())

    val timeEntriesForSelectedRange: Flow<List<DayStatistics>> = combine(
        timeEntryRepository.getAllTimeEntries(),
        activityRepository.allActiveActivities,
        selectedRange
    ) { entries, activities, range ->
        val activitiesMap = activities.associateBy { it.id }
        val startDateTime = range.startDate.atTime(range.startTime)
        val endDateTime = range.endDate.atTime(range.endTime)

        entries
            .filter { entry ->
                val entryEnd = entry.endTime ?: LocalDateTime.now()
                entry.startTime <= endDateTime && entryEnd >= startDateTime
            }
            .mapNotNull { entry ->
                activitiesMap[entry.activityId]?.let { activity ->
                    TimeEntryWithActivity(entry, activity)
                }
            }
            .groupBy { it.timeEntry.startTime.toLocalDate() }
            .map { (date, dayEntries) ->
                DayStatistics(date, dayEntries.sortedByDescending { it.timeEntry.startTime })
            }
            .sortedByDescending { it.date }
    }

    val activityStatistics: Flow<List<ActivityStatistics>> = combine(
        timeEntryRepository.getAllTimeEntries(),
        activityRepository.allActiveActivities,
        selectedRange
    ) { entries, activities, range ->
        val startDateTime = range.startDate.atTime(range.startTime)
        val endDateTime = range.endDate.atTime(range.endTime)
        val now = LocalDateTime.now()

        val stats = activities.map { activity ->
            val activityEntries = entries.filter { entry ->
                entry.activityId == activity.id &&
                    entry.startTime <= endDateTime &&
                    (entry.endTime ?: now) >= startDateTime
            }

            val totalDuration = activityEntries.sumOf { entry ->
                val effectiveStart = maxOf(entry.startTime, startDateTime)
                val effectiveEnd = minOf(entry.endTime ?: now, endDateTime)
                Duration.between(effectiveStart, effectiveEnd).seconds
            }

            ActivityStatistics(
                activity = activity,
                totalDuration = Duration.ofSeconds(totalDuration),
                entriesCount = activityEntries.size,
                percentage = 0f
            )
        }.filter { it.entriesCount > 0 }

        val totalSeconds = stats.sumOf { it.totalDuration.seconds }
        stats.map { stat ->
            stat.copy(
                percentage = if (totalSeconds > 0)
                    stat.totalDuration.seconds.toFloat() / totalSeconds.toFloat() * 100f
                else 0f
            )
        }.sortedByDescending { it.totalDuration }
    }

    fun setDateTimeRange(range: DateTimeRange) {
        _selectedRange.value = range
    }

    fun getTimeEntriesForPeriod(startDate: LocalDate, endDate: LocalDate): Flow<List<TimeEntryWithActivity>> {
        return combine(
            timeEntryRepository.getTimeEntriesForPeriod(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX)),
            activityRepository.allActiveActivities
        ) { entries, activities ->
            val activitiesMap = activities.associateBy { it.id }
            entries.mapNotNull { entry ->
                activitiesMap[entry.activityId]?.let { TimeEntryWithActivity(entry, it) }
            }.sortedByDescending { it.timeEntry.startTime }
        }
    }

    fun deleteEntriesInRange() {
        viewModelScope.launch {
            timeEntryRepository.deleteEntriesInRange(
                selectedRange.value.startDate.atTime(selectedRange.value.startTime),
                selectedRange.value.endDate.atTime(selectedRange.value.endTime)
            )
        }
    }

    class Factory(
        private val timeEntryRepository: TimeEntryRepository,
        private val activityRepository: ActivityRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StatisticsViewModel(timeEntryRepository, activityRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
