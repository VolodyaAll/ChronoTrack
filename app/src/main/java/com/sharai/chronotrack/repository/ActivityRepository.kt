package com.sharai.chronotrack.repository

import com.sharai.chronotrack.data.dao.ActivityDao
import com.sharai.chronotrack.data.model.Activity
import kotlinx.coroutines.flow.Flow

class ActivityRepository(private val activityDao: ActivityDao) {
    val allActiveActivities: Flow<List<Activity>> = activityDao.getAllActiveActivities()
    val archivedActivities: Flow<List<Activity>> = activityDao.getArchivedActivities()
    val currentActivity: Flow<Activity?> = activityDao.getCurrentActivity()

    suspend fun getActivityById(id: Long): Activity? = activityDao.getActivityById(id)

    suspend fun insertActivity(activity: Activity): Long = activityDao.insertActivity(activity)

    suspend fun updateActivity(activity: Activity) = activityDao.updateActivity(activity)

    suspend fun archiveActivity(activityId: Long) = activityDao.archiveActivity(activityId)

    suspend fun restoreActivity(activityId: Long) = activityDao.restoreActivity(activityId)

    suspend fun setCurrentActivity(activityId: Long) = activityDao.setCurrentActivity(activityId)

    suspend fun deleteActivity(activityId: Long) = activityDao.deleteActivity(activityId)
} 