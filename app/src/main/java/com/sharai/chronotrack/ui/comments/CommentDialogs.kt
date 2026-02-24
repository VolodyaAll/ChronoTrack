package com.sharai.chronotrack.ui.comments

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sharai.chronotrack.data.model.Comment
import com.sharai.chronotrack.data.model.MediaType

@Composable
fun AddCommentDialog(
    onDismiss: () -> Unit,
    onAddTextComment: (String) -> Unit,
    onAddPhotoComment: (String, Uri) -> Unit,
    onAddVideoComment: (String, Uri) -> Unit,
    onAddAudioComment: (String, Uri) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedMediaType by remember { mutableStateOf<MediaType?>(null) }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var showMediaOptions by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    fun createImageFile(): Uri? = try {
        val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        val file = java.io.File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        androidx.core.content.FileProvider.getUriForFile(context, "com.sharai.chronotrack.fileprovider", file)
    } catch (_: Exception) {
        null
    }

    fun createVideoFile(): Uri? = try {
        val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES)
        val file = java.io.File.createTempFile("VIDEO_${timeStamp}_", ".mp4", storageDir)
        androidx.core.content.FileProvider.getUriForFile(context, "com.sharai.chronotrack.fileprovider", file)
    } catch (_: Exception) {
        null
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedMediaUri = it
            selectedMediaType = MediaType.PHOTO
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempPhotoUri?.let {
                selectedMediaUri = it
                selectedMediaType = MediaType.PHOTO
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedMediaUri = it
            selectedMediaType = MediaType.VIDEO
        }
    }

    val takeVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            tempVideoUri?.let {
                selectedMediaUri = it
                selectedMediaType = MediaType.VIDEO
            }
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedMediaUri = it
            selectedMediaType = MediaType.AUDIO
        }
    }

    fun launchCamera() {
        try {
            createImageFile()?.let { photoUri ->
                tempPhotoUri = photoUri
                takePictureLauncher.launch(photoUri)
            }
        } catch (_: Exception) {
        }
    }

    fun launchVideoRecorder() {
        try {
            createVideoFile()?.let { videoUri ->
                tempVideoUri = videoUri
                takeVideoLauncher.launch(videoUri)
            }
        } catch (_: Exception) {
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[android.Manifest.permission.RECORD_AUDIO] ?: false
        if (cameraGranted && !audioGranted) launchCamera()
        else if (cameraGranted && audioGranted) launchVideoRecorder()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить комментарий") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Текст комментария") },
                    modifier = Modifier.fillMaxWidth()
                )

                selectedMediaUri?.let { uri ->
                    when (selectedMediaType) {
                        MediaType.PHOTO -> {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(uri).crossfade(true).build(),
                                    contentDescription = "Выбранное фото",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                )
                                IconButton(
                                    onClick = { selectedMediaUri = null; selectedMediaType = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(32.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(Icons.Default.Close, "Удалить фото", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        MediaType.VIDEO -> MediaChip(
                            icon = Icons.Default.VideoLibrary,
                            label = "Видео выбрано",
                            onRemove = { selectedMediaUri = null; selectedMediaType = null }
                        )
                        MediaType.AUDIO -> MediaChip(
                            icon = Icons.Default.MusicNote,
                            label = "Аудио выбрано",
                            onRemove = { selectedMediaUri = null; selectedMediaType = null }
                        )
                        else -> {}
                    }
                }

                if (selectedMediaUri == null) {
                    if (showMediaOptions) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MediaOptionRow(Icons.Default.Photo, "Выбрать фото из галереи") {
                                photoPickerLauncher.launch("image/*")
                            }
                            MediaOptionRow(Icons.Default.CameraAlt, "Сделать фото") {
                                permissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
                            }
                            MediaOptionRow(Icons.Default.VideoLibrary, "Выбрать видео из галереи") {
                                videoPickerLauncher.launch("video/*")
                            }
                            MediaOptionRow(Icons.Default.Videocam, "Записать видео") {
                                permissionLauncher.launch(
                                    arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
                                )
                            }
                            MediaOptionRow(Icons.Default.MusicNote, "Выбрать аудио из галереи") {
                                audioPickerLauncher.launch("audio/*")
                            }
                            TextButton(
                                onClick = { showMediaOptions = false },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Скрыть опции")
                            }
                        }
                    } else {
                        Button(onClick = { showMediaOptions = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить медиа")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Добавить медиа")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (selectedMediaType) {
                        MediaType.PHOTO -> selectedMediaUri?.let { onAddPhotoComment(text, it) }
                        MediaType.VIDEO -> selectedMediaUri?.let { onAddVideoComment(text, it) }
                        MediaType.AUDIO -> selectedMediaUri?.let { onAddAudioComment(text, it) }
                        null -> onAddTextComment(text)
                    }
                },
                enabled = text.isNotBlank() || selectedMediaUri != null
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun MediaOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label)
    }
}

@Composable
private fun MediaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, "Удалить", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun EditCommentDialog(
    comment: Comment,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(comment.text) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать комментарий") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Текст комментария") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }, enabled = text.isNotBlank()) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
fun MediaPreviewDialog(
    comment: Comment,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    val mediaUri = remember { android.net.Uri.parse(comment.mediaUri) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (comment.mediaType) {
                    MediaType.PHOTO -> "Просмотр фото"
                    MediaType.VIDEO -> "Просмотр видео"
                    MediaType.AUDIO -> "Прослушивание аудио"
                    else -> "Медиа"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (comment.text.isNotEmpty()) {
                    Text(text = comment.text, style = MaterialTheme.typography.bodyLarge)
                }

                when (comment.mediaType) {
                    MediaType.PHOTO -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(mediaUri).crossfade(true).build(),
                            contentDescription = "Фото",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    MediaType.VIDEO -> {
                        Button(onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(mediaUri, "video/*")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        }) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = "Воспроизвести видео")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Воспроизвести видео")
                        }
                    }
                    MediaType.AUDIO -> {
                        Button(onClick = {
                            if (isPlaying) {
                                mediaPlayer?.pause()
                                isPlaying = false
                            } else {
                                try {
                                    if (mediaPlayer == null) {
                                        mediaPlayer = android.media.MediaPlayer().apply {
                                            setDataSource(context, mediaUri)
                                            prepare()
                                            setOnCompletionListener { isPlaying = false }
                                        }
                                    }
                                    mediaPlayer?.start()
                                    isPlaying = true
                                } catch (_: Exception) {
                                }
                            }
                        }) {
                            Icon(
                                if (isPlaying) Icons.Default.Stop else Icons.Default.MusicNote,
                                contentDescription = if (isPlaying) "Остановить" else "Воспроизвести"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPlaying) "Остановить" else "Воспроизвести")
                        }
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}
