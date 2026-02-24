package com.sharai.chronotrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import com.sharai.chronotrack.ui.components.ActivityTile
import com.sharai.chronotrack.viewmodel.ActivityViewModel
import com.sharai.chronotrack.viewmodel.CommentViewModel
import com.sharai.chronotrack.viewmodel.TimeEntryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharai.chronotrack.ChronoTrackApp
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
    
    val context = LocalContext.current
    val app = context.applicationContext as ChronoTrackApp
    val timeEntryViewModel = viewModel<TimeEntryViewModel>(
        factory = TimeEntryViewModel.Factory(app.timeEntryRepository)
    )
    val commentViewModel = viewModel<CommentViewModel>(
        factory = CommentViewModel.Factory(app.commentRepository)
    )
    
    var showAddCommentDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ChronoTrack") },
            )
        },
        floatingActionButton = {
            if (currentActivity != null) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            val currentTimeEntry = timeEntryViewModel.getCurrentTimeEntry()
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