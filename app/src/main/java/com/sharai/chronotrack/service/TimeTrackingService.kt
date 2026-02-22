package com.sharai.chronotrack.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
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
        private const val TAG = "TimeTrackingService"
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
        startTracking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("Отслеживание активности..."))
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
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
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    private fun startTracking() {
        serviceScope.launch {
            // Проверяем наличие незавершенного периода активности
            val unfinishedEntry = timeEntryRepository.getAllTimeEntries().first()
                .find { it.endTime == null }
            
            if (unfinishedEntry != null) {
                currentTimeEntry = unfinishedEntry
                currentActivity = activityRepository.getActivityById(unfinishedEntry.activityId)
                
                currentActivity?.let { activity ->
                    activityRepository.setCurrentActivity(activity.id)
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, createNotification("Текущая активность: ${activity.name}"))
                }
            }

            // Больше не подписываемся на изменения активности
            // Вместо этого будем использовать прямой вызов handleActivityChange
        }
    }

    // Метод для смены активности с указанием времени начала
    fun changeActivity(newActivity: Activity?, startTime: LocalDateTime) {        
        serviceScope.launch {
            val currentEntry = timeEntryRepository.getCurrentTimeEntry()
            
            if (newActivity != null) {
                if (currentEntry != null) {
                    // Проверяем, совпадает ли выбранное время с временем начала текущей активности
                    if (currentEntry.startTime.toLocalDate() == startTime.toLocalDate() &&
                        currentEntry.startTime.hour == startTime.hour &&
                        currentEntry.startTime.minute == startTime.minute &&
                        currentEntry.startTime.second == startTime.second) {
                        // Если время начала совпадает, просто обновляем активность
                        timeEntryRepository.updateTimeEntry(currentEntry.copy(
                            activityId = newActivity.id
                        ))
                    } else {
                        // Если время начала отличается, завершаем текущую и начинаем новую
                        timeEntryRepository.updateTimeEntry(currentEntry.copy(
                            endTime = startTime,
                            duration = Duration.between(currentEntry.startTime, startTime)
                        ))
                        
                        // Создаем новую запись с выбранным временем начала
                        val timeEntry = TimeEntry(
                            activityId = newActivity.id,
                            startTime = startTime
                        )
                        val id = timeEntryRepository.insertTimeEntry(timeEntry)
                        currentTimeEntry = timeEntry.copy(id = id)
                    }
                } else {
                    // Если нет текущей записи, просто создаем новую
                    val timeEntry = TimeEntry(
                        activityId = newActivity.id,
                        startTime = startTime
                    )
                    val id = timeEntryRepository.insertTimeEntry(timeEntry)
                    currentTimeEntry = timeEntry.copy(id = id)
                }
                
                // Обновляем текущую активность
                currentActivity = newActivity
                
                // Обновляем уведомление
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, createNotification("Текущая активность: ${newActivity.name}"))
            }
        }
    }

    private suspend fun handleActivityChange(newActivity: Activity?) {
        val now = LocalDateTime.now()
        
        // Завершаем текущую запись времени
        currentTimeEntry?.let { entry ->            
            val endTime = now
            val duration = Duration.between(entry.startTime, endTime)
            timeEntryRepository.updateTimeEntry(entry.copy(
                endTime = endTime,
                duration = duration
            ))
            currentTimeEntry = null
        }

        // Создаем новую запись времени для новой активности
        newActivity?.let { activity ->
            // Проверяем, нет ли уже незавершенного периода для этой активности
            val existingEntry = timeEntryRepository.getAllTimeEntries().first()
                .find { it.endTime == null }

            if (existingEntry != null) {
                currentTimeEntry = existingEntry
            } else {
                val timeEntry = TimeEntry(
                    activityId = activity.id,
                    startTime = now
                )
                val id = timeEntryRepository.insertTimeEntry(timeEntry)
                currentTimeEntry = timeEntry.copy(id = id)
            }

            // Обновляем уведомление
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification("Текущая активность: ${activity.name}"))
        }

        currentActivity = newActivity
    }

    private fun formatDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        return buildString {
            if (hours > 0) {
                append(hours)
                append(" ч ")
            }
            if (minutes > 0 || hours == 0L) {
                append(minutes)
                append(" мин")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.launch {
            handleActivityChange(null)
        }
    }
} 