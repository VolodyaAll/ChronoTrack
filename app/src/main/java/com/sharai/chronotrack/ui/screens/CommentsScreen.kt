package com.sharai.chronotrack.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sharai.chronotrack.ChronoTrackApp
import com.sharai.chronotrack.data.model.Comment
import com.sharai.chronotrack.data.model.CommentWithActivityAndTimeEntry
import com.sharai.chronotrack.data.model.MediaType
import com.sharai.chronotrack.ui.comments.EditCommentDialog
import com.sharai.chronotrack.ui.comments.MediaPreviewDialog
import com.sharai.chronotrack.ui.utils.ColorUtils
import com.sharai.chronotrack.ui.utils.getIconByClassName
import com.sharai.chronotrack.viewmodel.ActivityViewModel
import com.sharai.chronotrack.viewmodel.CommentViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    onBack: () -> Unit,
    onTimeEntryClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as ChronoTrackApp

    val commentViewModel = viewModel<CommentViewModel>(
        factory = CommentViewModel.Factory(app.commentRepository)
    )
    val activityViewModel = viewModel<ActivityViewModel>(
        factory = ActivityViewModel.Factory(app.activityRepository, app.timeEntryRepository)
    )

    val allComments by commentViewModel.getAllCommentsWithActivityAndTimeEntry()
        .collectAsState(initial = emptyList())

    var showDeleteCommentDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    var commentToEdit by remember { mutableStateOf<Comment?>(null) }
    var showEditCommentDialog by remember { mutableStateOf(false) }
    var showMediaPreviewDialog by remember { mutableStateOf(false) }
    var mediaToPreview by remember { mutableStateOf<Comment?>(null) }

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedActivityFilter by remember { mutableStateOf<Long?>(null) }
    var selectedDateFilter by remember { mutableStateOf<String?>(null) }

    val allActivities by activityViewModel.allActiveActivities.collectAsState(initial = emptyList())

    val filteredComments = allComments.filter { commentWithData ->
        (selectedActivityFilter == null || commentWithData.activity.id == selectedActivityFilter) &&
        (selectedDateFilter == null || commentWithData.comment.createdAt.toLocalDate().toString() == selectedDateFilter)
    }

    val uniqueDates = allComments
        .map { it.comment.createdAt.toLocalDate().toString() }
        .distinct()
        .sorted()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Комментарии") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтр")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (allComments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет комментариев",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(filteredComments) { commentWithData ->
                    CommentWithActivityItem(
                        commentWithData = commentWithData,
                        onDeleteClick = {
                            commentToDelete = commentWithData.comment
                            showDeleteCommentDialog = true
                        },
                        onEditClick = {
                            if (commentWithData.comment.mediaType == null) {
                                commentToEdit = commentWithData.comment
                                showEditCommentDialog = true
                            }
                        },
                        onMediaClick = {
                            if (commentWithData.comment.mediaType != null) {
                                mediaToPreview = commentWithData.comment
                                showMediaPreviewDialog = true
                            }
                        },
                        onTimeEntryClick = {
                            onTimeEntryClick(commentWithData.timeEntry.id)
                        }
                    )
                }
            }
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Фильтр комментариев") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Активность:", style = MaterialTheme.typography.bodyLarge)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedActivityFilter = null }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedActivityFilter == null,
                                onClick = { selectedActivityFilter = null }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Все активности")
                        }

                        allActivities.forEach { activity ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedActivityFilter = activity.id }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedActivityFilter == activity.id,
                                    onClick = { selectedActivityFilter = activity.id }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(ColorUtils.getComposeColor(activity.color)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconByClassName(activity.icon),
                                        contentDescription = activity.name,
                                        tint = ColorUtils.getContrastColor(activity.color),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(activity.name)
                            }
                        }
                    }

                    HorizontalDivider()

                    Text("Дата:", style = MaterialTheme.typography.bodyLarge)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDateFilter = null }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDateFilter == null,
                                onClick = { selectedDateFilter = null }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Все даты")
                        }

                        uniqueDates.forEach { date ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDateFilter = date }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDateFilter == date,
                                    onClick = { selectedDateFilter = date }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(date.split("-").reversed().joinToString("."))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Применить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedActivityFilter = null
                        selectedDateFilter = null
                        showFilterDialog = false
                    }
                ) {
                    Text("Сбросить")
                }
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
            title = { Text("Удаление комментария") },
            text = { Text("Вы действительно хотите удалить этот комментарий?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        commentToDelete?.let { commentViewModel.deleteComment(it) }
                        showDeleteCommentDialog = false
                        commentToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteCommentDialog = false
                        commentToDelete = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun CommentWithActivityItem(
    commentWithData: CommentWithActivityAndTimeEntry,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onMediaClick: () -> Unit,
    onTimeEntryClick: () -> Unit
) {
    val context = LocalContext.current
    val timeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    val mediaUri = commentWithData.comment.mediaUri?.let { android.net.Uri.parse(it) }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ColorUtils.getComposeColor(commentWithData.activity.color).copy(alpha = 0.2f))
                    .clickable { onTimeEntryClick() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ColorUtils.getComposeColor(commentWithData.activity.color)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconByClassName(commentWithData.activity.icon),
                        contentDescription = commentWithData.activity.name,
                        tint = ColorUtils.getContrastColor(commentWithData.activity.color),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = commentWithData.activity.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    val startTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

                    Text(
                        text = "${commentWithData.timeEntry.startTime.format(dateFormatter)}, ${commentWithData.timeEntry.startTime.format(startTimeFormatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = commentWithData.comment.createdAt.format(timeFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить комментарий",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (commentWithData.comment.text.isNotEmpty()) {
                Text(
                    text = commentWithData.comment.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = commentWithData.comment.mediaType == null) { onEditClick() }
                        .padding(vertical = 4.dp)
                )
            }

            commentWithData.comment.mediaType?.let { mediaType ->
                when (mediaType) {
                    MediaType.PHOTO -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(mediaUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Фото",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
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
                                contentDescription = "Видео",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Нажмите, чтобы просмотреть видео",
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
                                contentDescription = "Аудио",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Нажмите, чтобы прослушать аудио",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
