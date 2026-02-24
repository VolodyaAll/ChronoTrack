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

class CommentViewModel(private val repository: CommentRepository) : ViewModel() {

    private val _selectedTimeEntryId = MutableStateFlow<Long?>(null)
    val selectedTimeEntryId: StateFlow<Long?> = _selectedTimeEntryId.asStateFlow()

    private val _selectedComment = MutableStateFlow<Comment?>(null)
    val selectedComment: StateFlow<Comment?> = _selectedComment.asStateFlow()

    fun setTimeEntry(timeEntryId: Long) {
        _selectedTimeEntryId.value = timeEntryId
        _selectedComment.value = null
    }

    fun getCommentsForCurrentTimeEntry(): Flow<List<Comment>>? {
        return _selectedTimeEntryId.value?.let { timeEntryId ->
            repository.getCommentsForTimeEntry(timeEntryId)
        }
    }

    fun getAllCommentsWithActivityAndTimeEntry(): Flow<List<CommentWithActivityAndTimeEntry>> =
        repository.getAllCommentsWithActivityAndTimeEntry()

    fun hasComments(timeEntryId: Long): Flow<Boolean> =
        repository.getCommentsForTimeEntry(timeEntryId).map { it.isNotEmpty() }

    fun loadComment(commentId: Long) {
        viewModelScope.launch {
            _selectedComment.value = repository.getCommentById(commentId)
        }
    }

    fun clearSelectedComment() {
        _selectedComment.value = null
    }

    fun addTextComment(text: String) {
        viewModelScope.launch {
            _selectedTimeEntryId.value?.let { timeEntryId ->
                repository.addTextComment(timeEntryId, text)
            }
        }
    }

    fun addPhotoComment(text: String, photoUri: Uri) {
        viewModelScope.launch {
            _selectedTimeEntryId.value?.let { timeEntryId ->
                repository.addMediaComment(timeEntryId, text, MediaType.PHOTO, photoUri.toString())
            }
        }
    }

    fun addVideoComment(text: String, videoUri: Uri) {
        viewModelScope.launch {
            _selectedTimeEntryId.value?.let { timeEntryId ->
                repository.addMediaComment(timeEntryId, text, MediaType.VIDEO, videoUri.toString())
            }
        }
    }

    fun addAudioComment(text: String, uri: Uri) {
        viewModelScope.launch {
            _selectedTimeEntryId.value?.let { timeEntryId ->
                repository.addMediaComment(timeEntryId, text, MediaType.AUDIO, uri.toString())
            }
        }
    }

    fun updateComment(comment: Comment) {
        viewModelScope.launch {
            repository.updateComment(comment)
        }
    }

    fun updateCommentText(commentId: Long, newText: String) {
        viewModelScope.launch {
            repository.getCommentById(commentId)?.let { comment ->
                repository.updateComment(comment.copy(text = newText))
            }
        }
    }

    fun deleteComment(comment: Comment) {
        viewModelScope.launch {
            repository.deleteComment(comment)
        }
    }

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
