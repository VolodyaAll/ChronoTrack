package com.sharai.chronotrack.data.dao

import androidx.room.*
import com.sharai.chronotrack.data.model.TimeEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TimeEntryDao {
    @Query("SELECT * FROM time_entries ORDER BY startTime DESC")
    fun getAllTimeEntries(): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE activityId = :activityId ORDER BY startTime DESC")
    fun getTimeEntriesForActivity(activityId: Long): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE startTime >= :startDate AND startTime < :endDate ORDER BY startTime DESC")
    fun getTimeEntriesForPeriod(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE endTime IS NULL LIMIT 1")
    suspend fun getCurrentTimeEntry(): TimeEntry?

    @Query("SELECT * FROM time_entries WHERE id = :id LIMIT 1")
    suspend fun getTimeEntryById(id: Long): TimeEntry?

    @Insert
    suspend fun insertTimeEntry(timeEntry: TimeEntry): Long

    @Update
    suspend fun updateTimeEntry(timeEntry: TimeEntry)

    @Delete
    suspend fun deleteTimeEntry(timeEntry: TimeEntry)

    @Query("DELETE FROM time_entries WHERE activityId = :activityId")
    suspend fun deleteTimeEntriesForActivity(activityId: Long)

    @Query("DELETE FROM time_entries WHERE startTime >= :startDateTime AND (endTime IS NULL OR endTime <= :endDateTime)")
    suspend fun deleteEntriesInRange(startDateTime: LocalDateTime, endDateTime: LocalDateTime)

    @Query("SELECT MIN(startTime) FROM time_entries")
    fun getFirstActivityDate(): Flow<LocalDateTime?>
} 