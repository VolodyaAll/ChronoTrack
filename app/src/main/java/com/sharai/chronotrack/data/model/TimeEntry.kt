package com.sharai.chronotrack.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.LocalDateTime

@Entity(
    tableName = "time_entries",
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("activityId")]
)
data class TimeEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val duration: Duration? = null
) 