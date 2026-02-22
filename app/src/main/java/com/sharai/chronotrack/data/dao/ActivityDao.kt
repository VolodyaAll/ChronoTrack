package com.sharai.chronotrack.data.dao

import androidx.room.*
import com.sharai.chronotrack.data.model.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE isArchived = 0")
    fun getAllActiveActivities(): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE isArchived = 1")
    fun getArchivedActivities(): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Long): Activity?

    @Query("SELECT * FROM activities WHERE isActive = 1 AND isArchived = 0 LIMIT 1")
    fun getCurrentActivity(): Flow<Activity?>

    @Insert
    suspend fun insertActivity(activity: Activity): Long

    @Update
    suspend fun updateActivity(activity: Activity)

    @Query("UPDATE activities SET isActive = CASE WHEN id = :activityId THEN 1 ELSE 0 END")
    suspend fun setCurrentActivity(activityId: Long)

    @Query("UPDATE activities SET isArchived = 1 WHERE id = :activityId")
    suspend fun archiveActivity(activityId: Long)

    @Query("UPDATE activities SET isArchived = 0 WHERE id = :activityId")
    suspend fun restoreActivity(activityId: Long)

    @Query("DELETE FROM activities WHERE id = :activityId")
    suspend fun deleteActivity(activityId: Long)
} 