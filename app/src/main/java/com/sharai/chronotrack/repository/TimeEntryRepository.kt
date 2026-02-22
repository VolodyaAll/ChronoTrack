package com.sharai.chronotrack.repository

import com.sharai.chronotrack.data.dao.TimeEntryDao
import com.sharai.chronotrack.data.model.TimeEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.LocalDate

class TimeEntryRepository(private val timeEntryDao: TimeEntryDao) {
    fun getAllTimeEntries(): Flow<List<TimeEntry>> = timeEntryDao.getAllTimeEntries()

    fun getTimeEntriesForActivity(activityId: Long): Flow<List<TimeEntry>> =
        timeEntryDao.getTimeEntriesForActivity(activityId)

    fun getTimeEntriesForPeriod(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<TimeEntry>> =
        timeEntryDao.getTimeEntriesForPeriod(startDate, endDate)

    suspend fun getCurrentTimeEntry(): TimeEntry? = timeEntryDao.getCurrentTimeEntry()

    suspend fun getTimeEntryById(id: Long): TimeEntry? = timeEntryDao.getTimeEntryById(id)

    suspend fun insertTimeEntry(timeEntry: TimeEntry): Long = timeEntryDao.insertTimeEntry(timeEntry)

    suspend fun updateTimeEntry(timeEntry: TimeEntry) = timeEntryDao.updateTimeEntry(timeEntry)

    suspend fun deleteTimeEntry(timeEntry: TimeEntry) = timeEntryDao.deleteTimeEntry(timeEntry)

    suspend fun deleteTimeEntriesForActivity(activityId: Long) =
        timeEntryDao.deleteTimeEntriesForActivity(activityId)

    suspend fun deleteEntriesInRange(startDateTime: LocalDateTime, endDateTime: LocalDateTime) {
        timeEntryDao.deleteEntriesInRange(startDateTime, endDateTime)
    }

    fun getFirstActivityDate(): Flow<LocalDate> =
        timeEntryDao.getFirstActivityDate().map { dateTime ->
            dateTime?.toLocalDate() ?: LocalDate.now()
        }
} 