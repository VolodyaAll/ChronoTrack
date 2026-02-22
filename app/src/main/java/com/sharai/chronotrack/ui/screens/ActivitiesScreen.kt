package com.sharai.chronotrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import com.sharai.chronotrack.data.model.Activity
import com.sharai.chronotrack.ui.components.ActivityTile
import com.sharai.chronotrack.viewmodel.ActivityViewModel
import com.sharai.chronotrack.viewmodel.CommentViewModel
import com.sharai.chronotrack.viewmodel.TimeEntryViewModel
import com.sharai.chronotrack.ui.utils.getIconByClassName
import com.sharai.chronotrack.ui.utils.ColorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharai.chronotrack.ChronoTrackApp
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.sharai.chronotrack.ui.comments.AddCommentDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    viewModel: ActivityViewModel,
    onEditActivity: (Long) -> Unit,
    onTimeEntryClick: (Long) -> Unit,
    onCommentsClick: () -> Unit = {}
) {
    val activities by viewModel.allActiveActivities.collectAsState(initial = emptyList())
    val currentActivity by viewModel.currentActivity.collectAsState(initial = null)
    val currentActivityStartTime by viewModel.currentActivityStartTime.collectAsState(initial = null)
    
    // Получаем доступ к LocalContext для доступа к приложению
    val context = LocalContext.current
    val app = context.applicationContext as ChronoTrackApp
    
    // Создаем TimeEntryViewModel с использованием фабрики
    val timeEntryViewModel = viewModel<TimeEntryViewModel>(
        factory = TimeEntryViewModel.Factory(app.timeEntryRepository)
    )
    
    // Создаем CommentViewModel с использованием фабрики
    val commentViewModel = viewModel<CommentViewModel>(
        factory = CommentViewModel.Factory(app.commentRepository)
    )
    
    // Состояние для диалога добавления комментария
    var showAddCommentDialog by remember { mutableStateOf(false) }
    
    // Создаем CoroutineScope для запуска корутин
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ChronoTrack") },
            )
        },
        floatingActionButton = {
            // Показываем FAB только если есть текущая активность
            if (currentActivity != null) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            // Получаем текущую запись времени
                            val currentTimeEntry = timeEntryViewModel.getCurrentTimeEntry()
                            
                            // Если текущая запись времени существует, устанавливаем ее в CommentViewModel
                            if (currentTimeEntry != null) {
                                commentViewModel.setTimeEntry(currentTimeEntry.id)
                                showAddCommentDialog = true
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Добавить комментарий к текущей активности"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Сетка активностей
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(activities) { activity ->
                    ActivityTile(
                        activity = activity,
                        isActive = activity.id == currentActivity?.id,
                        currentActivityStartTime = currentActivityStartTime,
                        onActivityClick = { viewModel.setCurrentActivity(activity.id, LocalDateTime.now()) },
                        onActivityStartTimeChange = { newStartTime -> 
                            viewModel.setCurrentActivity(activity.id, newStartTime)
                        },
                        onEditClick = { onEditActivity(activity.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Кнопка добавления новой активности
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onEditActivity(-1) }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить активность",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Диалог добавления комментария
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
}

@Composable
fun CurrentActivityCard(activity: Activity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
            Column {
                Text(
                    text = "Текущая активность",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityItem(
    activity: Activity,
    isActive: Boolean,
    onActivityClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onActivityClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ColorUtils.getComposeColor(activity.color)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconByClassName(activity.icon),
                        contentDescription = activity.name,
                        tint = ColorUtils.getContrastColor(activity.color),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Редактировать"
                )
            }
        }
    }
}

@Composable
fun TimeEntryItem(
    timeEntryId: Long,
    activity: Activity,
    startTime: LocalDateTime,
    endTime: LocalDateTime?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as ChronoTrackApp
    
    val commentViewModel = viewModel<CommentViewModel>(
        factory = CommentViewModel.Factory(app.commentRepository)
    )
    
    val hasComments by commentViewModel.hasComments(timeEntryId).collectAsState(initial = false)
    
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    val duration = if (endTime != null) {
        Duration.between(startTime, endTime)
    } else {
        Duration.between(startTime, LocalDateTime.now())
    }
    
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()
    val durationText = if (hours > 0) "${hours}ч ${minutes}мин" else "${minutes}мин"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = ColorUtils.getComposeColor(activity.color),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconByClassName(activity.icon),
                            contentDescription = activity.name,
                            tint = ColorUtils.getContrastColor(activity.color),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    if (hasComments) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Есть комментарии",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Время начала и окончания
            Text(
                text = when {
                    endTime == null -> {
                        "${startTime.format(timeFormatter)} - настоящее время"
                    }
                    startTime.toLocalDate() == endTime.toLocalDate() -> {
                        "${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}"
                    }
                    else -> {
                        "${startTime.format(dateFormatter)} ${startTime.format(timeFormatter)} - ${endTime.format(dateFormatter)} ${endTime.format(timeFormatter)}"
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
} 