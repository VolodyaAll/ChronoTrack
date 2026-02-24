package com.sharai.chronotrack.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sharai.chronotrack.R
import com.sharai.chronotrack.data.model.Activity
import com.sharai.chronotrack.data.model.Comment
import com.sharai.chronotrack.data.model.MediaType
import com.sharai.chronotrack.data.model.TimeEntry
import com.sharai.chronotrack.ui.utils.ColorUtils
import com.sharai.chronotrack.ui.utils.getIconByClassName
import com.sharai.chronotrack.viewmodel.CommentViewModel
import com.sharai.chronotrack.ui.comments.AddCommentDialog
import com.sharai.chronotrack.ui.comments.EditCommentDialog
import com.sharai.chronotrack.ui.comments.MediaPreviewDialog
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeEntryDetailsScreen(
    timeEntry: TimeEntry,
    activity: Activity,
    commentViewModel: CommentViewModel,
    onBack: () -> Unit
) {
    val comments by commentViewModel.getCommentsForCurrentTimeEntry()?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList<Comment>()) }
    var showAddCommentDialog by remember { mutableStateOf(false) }
    var showDeleteCommentDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    var commentToEdit by remember { mutableStateOf<Comment?>(null) }
    var showEditCommentDialog by remember { mutableStateOf(false) }
    var showMediaPreviewDialog by remember { mutableStateOf(false) }
    var mediaToPreview by remember { mutableStateOf<Comment?>(null) }
    var showDeleteTimeEntryDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val app = context.applicationContext as com.sharai.chronotrack.ChronoTrackApp
    val timeEntryViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.sharai.chronotrack.viewmodel.TimeEntryViewModel>(
        factory = com.sharai.chronotrack.viewmodel.TimeEntryViewModel.Factory(app.timeEntryRepository)
    )
    
    LaunchedEffect(timeEntry.id) {
        commentViewModel.setTimeEntry(timeEntry.id)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.activity_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteTimeEntryDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_time_entry),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCommentDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_comment))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ActivityInfoCard(activity, timeEntry)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.comments_count, comments.size),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            if (comments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_comments),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(comments) { comment ->
                    CommentItem(
                        comment = comment,
                        onDeleteClick = {
                            commentToDelete = comment
                            showDeleteCommentDialog = true
                        },
                        onEditClick = {
                            if (comment.mediaType == null) {
                                commentToEdit = comment
                                showEditCommentDialog = true
                            }
                        },
                        onMediaClick = {
                            if (comment.mediaType != null) {
                                mediaToPreview = comment
                                showMediaPreviewDialog = true
                            }
                        }
                    )
                }
            }
        }
    }
    
    if (showAddCommentDialog) {
        AddCommentDialog(
            onDismiss = { showAddCommentDialog = false },
            onAddTextComment = { text ->
                commentViewModel.addTextComment(text)
                showAddCommentDialog = false
            },
            onAddPhotoComment = { text, uri ->
                commentViewModel.addPhotoComment(text, uri)
                showAddCommentDialog = false
            },
            onAddVideoComment = { text, uri ->
                commentViewModel.addVideoComment(text, uri)
                showAddCommentDialog = false
            },
            onAddAudioComment = { text, uri ->
                commentViewModel.addAudioComment(text, uri)
                showAddCommentDialog = false
            }
        )
    }
    
    if (showEditCommentDialog && commentToEdit != null) {
        EditCommentDialog(
            comment = commentToEdit!!,
            onDismiss = { 
                showEditCommentDialog = false
                commentToEdit = null
            },
            onSave = { text ->
                commentViewModel.updateCommentText(commentToEdit!!.id, text)
                showEditCommentDialog = false
                commentToEdit = null
            }
        )
    }
    
    if (showMediaPreviewDialog && mediaToPreview != null) {
        MediaPreviewDialog(
            comment = mediaToPreview!!,
            onDismiss = { 
                showMediaPreviewDialog = false
                mediaToPreview = null
            }
        )
    }
    
    if (showDeleteCommentDialog && commentToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteCommentDialog = false
                commentToDelete = null
            },
            title = { Text(stringResource(R.string.delete_comment)) },
            text = { Text(stringResource(R.string.delete_comment_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        commentToDelete?.let { commentViewModel.deleteComment(it) }
                        showDeleteCommentDialog = false
                        commentToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteCommentDialog = false
                        commentToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    if (showDeleteTimeEntryDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTimeEntryDialog = false },
            title = { Text(stringResource(R.string.delete_time_entry)) },
            text = { 
                Column {
                    Text(stringResource(R.string.delete_time_entry_confirmation))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.delete_time_entry_comments_warning),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        timeEntryViewModel.deleteTimeEntry(timeEntry)
                        showDeleteTimeEntryDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteTimeEntryDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ActivityInfoCard(activity: Activity, timeEntry: TimeEntry) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    val startDate = timeEntry.startTime.format(dateFormatter)
    val startTime = timeEntry.startTime.format(timeFormatter)
    
    val endDate = timeEntry.endTime?.format(dateFormatter) ?: stringResource(R.string.in_progress)
    val endTime = timeEntry.endTime?.format(timeFormatter) ?: "--:--"
    
    val duration = if (timeEntry.endTime != null) {
        val duration = Duration.between(timeEntry.startTime, timeEntry.endTime)
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        stringResource(R.string.hours_minutes_format, hours, minutes)
    } else {
        val duration = Duration.between(timeEntry.startTime, LocalDateTime.now())
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        stringResource(
            R.string.in_progress_duration, 
            stringResource(R.string.hours_minutes_format, hours, minutes)
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ColorUtils.getComposeColor(activity.color)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconByClassName(activity.icon),
                        contentDescription = activity.name,
                        tint = ColorUtils.getContrastColor(activity.color),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.start),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$startDate, $startTime",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.end),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$endDate, $endTime",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.duration_format, duration),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onMediaClick: () -> Unit
) {
    val context = LocalContext.current
    val timeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    val mediaUri = comment.mediaUri?.let { android.net.Uri.parse(it) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.createdAt.format(timeFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_comment),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (comment.text.isNotEmpty()) {
                Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = comment.mediaType == null) { onEditClick() }
                        .padding(vertical = 4.dp)
                )
            }
            
            comment.mediaType?.let { mediaType ->
                when (mediaType) {
                    MediaType.PHOTO -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(mediaUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.photo_preview),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onMediaClick() }
                        )
                    }
                    MediaType.VIDEO -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onMediaClick() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = stringResource(R.string.video_preview),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = stringResource(R.string.tap_to_view_video),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    MediaType.AUDIO -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onMediaClick() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = stringResource(R.string.audio_preview),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = stringResource(R.string.tap_to_play_audio),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
} 