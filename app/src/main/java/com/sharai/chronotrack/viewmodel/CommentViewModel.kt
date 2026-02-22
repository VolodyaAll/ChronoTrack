package com.sharai.chronotrack.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sharai.chronotrack.data.model.Comment
import com.sharai.chronotrack.data.model.CommentWithActivityAndTimeEntry
import com.sharai.chronotrack.data.model.MediaType
import com.sharai.chronotrack.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * ViewModel для работы с комментариями к периодам активности
 */
class CommentViewModel(private val repository: CommentRepository) : ViewModel() {
    
    private val _selectedTimeEntryId = MutableStateFlow<Long?>(null)
    val selectedTimeEntryId: StateFlow<Long?> = _selectedTimeEntryId.asStateFlow()
    
    private val _selectedComment = MutableStateFlow<Comment?>(null)
    val selectedComment: StateFlow<Comment?> = _selectedComment.asStateFlow()
    
    /**
     * Установить текущую запись времени для работы с комментариями
     */
    fun setTimeEntry(timeEntryId: Long) {
        _selectedTimeEntryId.value = timeEntryId
        _selectedComment.value = null
    }
    
    /**
     * Получить комментарии для текущей записи времени
     */
    fun getCommentsForCurrentTimeEntry(): Flow<List<Comment>>? {
        return _selectedTimeEntryId.value?.let { timeEntryId ->
            repository.getCommentsForTimeEntry(timeEntryId)
        }
    }
    
    /**
     * Получить все комментарии с информацией об активностях и записях времени
     */
    fun getAllCommentsWithActivityAndTimeEntry(): Flow<List<CommentWithActivityAndTimeEntry>> {
        return repository.getAllCommentsWithActivityAndTimeEntry()
    }
    
    /**
     * Проверить наличие комментариев у записи времени
     */
    fun hasComments(timeEntryId: Long): Flow<Boolean> {
        return repository.getCommentsForTimeEntry(timeEntryId)
            .map { comments -> comments.isNotEmpty() }
    }
    
    /**
     * Загрузить комментарий по ID
     */
    fun loadComment(commentId: Long) {
        viewModelScope.launch {
            _selectedComment.value = repository.getCommentById(commentId)
        }
    }
    
    /**
     * Очистить выбранный комментарий
     */
    fun clearSelectedComment() {
        _selectedComment.value = null
    }
    
    /**
     * Добавить текстовый комментарий
     */
    fun addTextComment(text: String) {
        viewModelScope.launch {
            _selectedTimeEntryId.value?.let { timeEntryId ->
                repository.addTextComment(timeEntryId, text)
            }
        }
    }
    
    /**
     * Добавить комментарий с фото
     */
    fun addPhotoComment(text: String, photoUri: Uri) {
        viewModelScope.launch {
            _selectedTimeEntryId.value?.let { timeEntryId ->
                repository.addMediaComment(timeEntryId, text, MediaType.PHOTO, photoUri.toString())
            }
        }
    }
    
    /**
     * Добавить комментарий с видео
     */
    fun addVideoComment(text: String, videoUri: Uri) {
        viewModelScope.launch {
            _selectedTimeEntryId.value?.let { timeEntryId ->
                repository.addMediaComment(timeEntryId, text, MediaType.VIDEO, videoUri.toString())
            }
        }
    }
    
    /**
     * Добавить комментарий с аудио
     */
    fun addAudioComment(text: String, uri: Uri) {
        viewModelScope.launch {
            _selectedTimeEntryId.value?.let { timeEntryId ->
                repository.addMediaComment(timeEntryId, text, MediaType.AUDIO, uri.toString())
            }
        }
    }
    
    /**
     * Обновить комментарий
     */
    fun updateComment(comment: Comment) {
        viewModelScope.launch {
            repository.updateComment(comment)
        }
    }
    
    /**
     * Обновить текст комментария
     */
    fun updateCommentText(commentId: Long, newText: String) {
        viewModelScope.launch {
            repository.getCommentById(commentId)?.let { comment ->
                val updatedComment = comment.copy(text = newText)
                repository.updateComment(updatedComment)
            }
        }
    }
    
    /**
     * Удалить комментарий
     */
    fun deleteComment(comment: Comment) {
        viewModelScope.launch {
            repository.deleteComment(comment)
        }
    }
    
    /**
     * Фабрика для создания ViewModel
     */
    class Factory(private val repository: CommentRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CommentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CommentViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 