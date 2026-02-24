package com.sharai.chronotrack.repository

import com.sharai.chronotrack.data.dao.CommentDao
import com.sharai.chronotrack.data.model.Comment
import com.sharai.chronotrack.data.model.CommentWithActivityAndTimeEntry
import com.sharai.chronotrack.data.model.MediaType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class CommentRepository(private val commentDao: CommentDao) {

    fun getCommentsForTimeEntry(timeEntryId: Long): Flow<List<Comment>> =
        commentDao.getCommentsForTimeEntry(timeEntryId)

    suspend fun getCommentById(commentId: Long): Comment? =
        commentDao.getCommentById(commentId)

    suspend fun addTextComment(timeEntryId: Long, text: String): Long {
        val comment = Comment(
            timeEntryId = timeEntryId,
            text = text,
            createdAt = LocalDateTime.now()
        )
        return commentDao.insertComment(comment)
    }

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

    suspend fun updateComment(comment: Comment) =
        commentDao.updateComment(comment)

    suspend fun deleteComment(comment: Comment) =
        commentDao.deleteComment(comment)

    suspend fun deleteCommentsForTimeEntry(timeEntryId: Long) =
        commentDao.deleteCommentsForTimeEntry(timeEntryId)

    fun getAllCommentsWithActivityAndTimeEntry(): Flow<List<CommentWithActivityAndTimeEntry>> =
        commentDao.getAllCommentsWithDetails()
}
