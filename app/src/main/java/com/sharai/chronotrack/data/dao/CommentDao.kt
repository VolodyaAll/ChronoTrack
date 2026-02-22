package com.sharai.chronotrack.data.dao

import androidx.room.*
import com.sharai.chronotrack.data.model.Comment
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с комментариями к периодам активности
 */
@Dao
interface CommentDao {
    /**
     * Получить все комментарии для указанной записи времени
     */
    @Query("SELECT * FROM comments WHERE timeEntryId = :timeEntryId ORDER BY createdAt DESC")
    fun getCommentsForTimeEntry(timeEntryId: Long): Flow<List<Comment>>
    
    /**
     * Получить комментарий по ID
     */
    @Query("SELECT * FROM comments WHERE id = :commentId")
    suspend fun getCommentById(commentId: Long): Comment?
    
    /**
     * Добавить новый комментарий
     */
    @Insert
    suspend fun insertComment(comment: Comment): Long
    
    /**
     * Обновить существующий комментарий
     */
    @Update
    suspend fun updateComment(comment: Comment)
    
    /**
     * Удалить комментарий
     */
    @Delete
    suspend fun deleteComment(comment: Comment)
    
    /**
     * Удалить все комментарии для указанной записи времени
     */
    @Query("DELETE FROM comments WHERE timeEntryId = :timeEntryId")
    suspend fun deleteCommentsForTimeEntry(timeEntryId: Long)
    
    /**
     * Получить все комментарии с информацией о связанных записях времени и активностях
     */
    @Query("""
        SELECT c.* FROM comments c
        JOIN time_entries t ON c.timeEntryId = t.id
        ORDER BY c.createdAt DESC
    """)
    fun getAllComments(): Flow<List<Comment>>
    
    /**
     * Получить запись времени для комментария
     */
    @Query("SELECT * FROM time_entries WHERE id = :timeEntryId")
    suspend fun getTimeEntryForComment(timeEntryId: Long): com.sharai.chronotrack.data.model.TimeEntry
    
    /**
     * Получить активность для записи времени
     */
    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityForTimeEntry(activityId: Long): com.sharai.chronotrack.data.model.Activity
} 