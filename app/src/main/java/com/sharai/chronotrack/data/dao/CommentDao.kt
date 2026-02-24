package com.sharai.chronotrack.data.dao

import androidx.room.*
import com.sharai.chronotrack.data.model.Comment
import com.sharai.chronotrack.data.model.CommentWithActivityAndTimeEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    @Query("SELECT * FROM comments WHERE timeEntryId = :timeEntryId ORDER BY createdAt DESC")
    fun getCommentsForTimeEntry(timeEntryId: Long): Flow<List<Comment>>

    @Query("SELECT * FROM comments WHERE id = :commentId")
    suspend fun getCommentById(commentId: Long): Comment?

    @Insert
    suspend fun insertComment(comment: Comment): Long

    @Update
    suspend fun updateComment(comment: Comment)

    @Delete
    suspend fun deleteComment(comment: Comment)

    @Query("DELETE FROM comments WHERE timeEntryId = :timeEntryId")
    suspend fun deleteCommentsForTimeEntry(timeEntryId: Long)

    @Transaction
    @Query("""
        SELECT 
            c.id, c.timeEntryId, c.text, c.mediaType, c.mediaUri, c.createdAt,
            t.id AS te_id, t.activityId AS te_activityId, 
            t.startTime AS te_startTime, t.endTime AS te_endTime, t.duration AS te_duration,
            a.id AS a_id, a.name AS a_name, a.color AS a_color, 
            a.icon AS a_icon, a.isActive AS a_isActive, a.isArchived AS a_isArchived
        FROM comments c
        INNER JOIN time_entries t ON c.timeEntryId = t.id
        INNER JOIN activities a ON t.activityId = a.id
        ORDER BY c.createdAt DESC
    """)
    fun getAllCommentsWithDetails(): Flow<List<CommentWithActivityAndTimeEntry>>
}
