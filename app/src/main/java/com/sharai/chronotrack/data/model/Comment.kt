package com.sharai.chronotrack.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Модель данных для комментариев к периодам активности
 * Комментарии могут содержать текст и ссылки на медиафайлы (аудио, фото, видео)
 */
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
    
    // ID записи времени, к которой относится комментарий
    val timeEntryId: Long,
    
    // Текст комментария
    val text: String = "",
    
    // Тип медиафайла (если есть): PHOTO, VIDEO, AUDIO или null
    val mediaType: MediaType? = null,
    
    // URI медиафайла (если есть)
    val mediaUri: String? = null,
    
    // Время создания комментария
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Типы медиафайлов для комментариев
 */
enum class MediaType {
    PHOTO, VIDEO, AUDIO
} 