package com.sharai.chronotrack.repository

import com.sharai.chronotrack.data.dao.CommentDao
import com.sharai.chronotrack.data.model.Comment
import com.sharai.chronotrack.data.model.CommentWithActivityAndTimeEntry
import com.sharai.chronotrack.data.model.MediaType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

/**
 * Репозиторий для работы с комментариями к периодам активности
 */
class CommentRepository(private val commentDao: CommentDao) {
    
    /**
     * Получить все комментарии для указанной записи времени
     */
    fun getCommentsForTimeEntry(timeEntryId: Long): Flow<List<Comment>> {
        return commentDao.getCommentsForTimeEntry(timeEntryId)
    }
    
    /**
     * Получить комментарий по ID
     */
    suspend fun getCommentById(commentId: Long): Comment? {
        return commentDao.getCommentById(commentId)
    }
    
    /**
     * Добавить текстовый комментарий
     */
    suspend fun addTextComment(timeEntryId: Long, text: String): Long {
        val comment = Comment(
            timeEntryId = timeEntryId,
            text = text,
            createdAt = LocalDateTime.now()
        )
        return commentDao.insertComment(comment)
    }
    
    /**
     * Добавить комментарий с медиафайлом
     */
    suspend fun addMediaComment(timeEntryId: Long, text: String, mediaType: MediaType, mediaUri: String): Long {
        val comment = Comment(
            timeEntryId = timeEntryId,
            text = text,
            mediaType = mediaType,
            mediaUri = mediaUri,
            createdAt = LocalDateTime.now()
        )
        return commentDao.insertComment(comment)
    }
    
    /**
     * Обновить комментарий
     */
    suspend fun updateComment(comment: Comment) {
        commentDao.updateComment(comment)
    }
    
    /**
     * Удалить комментарий
     */
    suspend fun deleteComment(comment: Comment) {
        commentDao.deleteComment(comment)
    }
    
    /**
     * Удалить все комментарии для указанной записи времени
     */
    suspend fun deleteCommentsForTimeEntry(timeEntryId: Long) {
        commentDao.deleteCommentsForTimeEntry(timeEntryId)
    }
    
    /**
     * Получить все комментарии с информацией об активностях и записях времени
     */
    fun getAllCommentsWithActivityAndTimeEntry(): Flow<List<CommentWithActivityAndTimeEntry>> {
        return commentDao.getAllComments().map { comments ->
            comments.map { comment ->
                val timeEntry = commentDao.getTimeEntryForComment(comment.timeEntryId)
                val activity = commentDao.getActivityForTimeEntry(timeEntry.activityId)
                CommentWithActivityAndTimeEntry(
                    comment = comment,
                    timeEntry = timeEntry,
                    activity = activity
                )
            }
        }
    }
} 