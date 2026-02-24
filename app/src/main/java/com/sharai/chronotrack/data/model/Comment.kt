package com.sharai.chronotrack.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = TimeEntry::class,
            parentColumns = ["id"],
            childColumns = ["timeEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("timeEntryId")]
)
data class Comment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timeEntryId: Long,
    val text: String = "",
    val mediaType: MediaType? = null,
    val mediaUri: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class MediaType {
    PHOTO, VIDEO, AUDIO
}
