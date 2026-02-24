package com.sharai.chronotrack.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sharai.chronotrack.MainActivity
import com.sharai.chronotrack.R
import com.sharai.chronotrack.data.model.Activity
import com.sharai.chronotrack.data.model.TimeEntry
import com.sharai.chronotrack.repository.ActivityRepository
import com.sharai.chronotrack.repository.TimeEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.Duration

class TimeTrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var activityRepository: ActivityRepository
    private lateinit var timeEntryRepository: TimeEntryRepository
    private var currentActivity: Activity? = null
    private var currentTimeEntry: TimeEntry? = null

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "time_tracking_channel"
        private const val CHANNEL_NAME = "Отслеживание времени"

        private var instance: TimeTrackingService? = null
        fun getInstance(): TimeTrackingService? = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        val app = application as com.sharai.chronotrack.ChronoTrackApp
        activityRepository = app.activityRepository
        timeEntryRepository = app.timeEntryRepository
        createNotificationChannel()
        restoreTrackingState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("Отслеживание активности..."))
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Канал для отслеживания времени активностей"
                setShowBadge(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("ChronoTrack")
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_timer)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    private fun restoreTrackingState() {
        serviceScope.launch {
            val unfinishedEntry = timeEntryRepository.getAllTimeEntries().first()
                .find { it.endTime == null }

            if (unfinishedEntry != null) {
                currentTimeEntry = unfinishedEntry
                currentActivity = activityRepository.getActivityById(unfinishedEntry.activityId)
                currentActivity?.let { activity ->
                    activityRepository.setCurrentActivity(activity.id)
                    updateNotification("Текущая активность: ${activity.name}")
                }
            }
        }
    }

    fun changeActivity(newActivity: Activity?, startTime: LocalDateTime) {
        serviceScope.launch {
            if (newActivity == null) return@launch

            val currentEntry = timeEntryRepository.getCurrentTimeEntry()

            if (currentEntry != null) {
                val sameStartTime = currentEntry.startTime.toLocalDate() == startTime.toLocalDate() &&
                    currentEntry.startTime.hour == startTime.hour &&
                    currentEntry.startTime.minute == startTime.minute &&
                    currentEntry.startTime.second == startTime.second

                if (sameStartTime) {
                    // Same start time — just reassign the activity (correcting a misclick)
                    timeEntryRepository.updateTimeEntry(currentEntry.copy(activityId = newActivity.id))
                } else {
                    timeEntryRepository.updateTimeEntry(
                        currentEntry.copy(
                            endTime = startTime,
                            duration = Duration.between(currentEntry.startTime, startTime)
                        )
                    )
                    val timeEntry = TimeEntry(activityId = newActivity.id, startTime = startTime)
                    val id = timeEntryRepository.insertTimeEntry(timeEntry)
                    currentTimeEntry = timeEntry.copy(id = id)
                }
            } else {
                val timeEntry = TimeEntry(activityId = newActivity.id, startTime = startTime)
                val id = timeEntryRepository.insertTimeEntry(timeEntry)
                currentTimeEntry = timeEntry.copy(id = id)
            }

            currentActivity = newActivity
            updateNotification("Текущая активность: ${newActivity.name}")
        }
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.launch {
            currentTimeEntry?.let { entry ->
                val now = LocalDateTime.now()
                timeEntryRepository.updateTimeEntry(
                    entry.copy(endTime = now, duration = Duration.between(entry.startTime, now))
                )
                currentTimeEntry = null
            }
            currentActivity = null
        }
    }
}
