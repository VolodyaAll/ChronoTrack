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

/**
 * Диалог для добавления комментария с возможностью прикрепления медиа-файлов
 */
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
    
    // Функция для создания временного файла для медиа с использованием FileProvider
    fun createImageFile(): Uri? {
        return try {
            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            val file = java.io.File.createTempFile(
                "JPEG_${timeStamp}_", /* префикс */
                ".jpg", /* суффикс */
                storageDir /* директория */
            )
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "com.sharai.chronotrack.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun createVideoFile(): Uri? {
        return try {
            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES)
            val file = java.io.File.createTempFile(
                "VIDEO_${timeStamp}_", /* префикс */
                ".mp4", /* суффикс */
                storageDir /* директория */
            )
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "com.sharai.chronotrack.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // Запуск выбора существующего фото
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedMediaUri = it
            selectedMediaType = MediaType.PHOTO
        }
    }
    
    // Запуск камеры для создания фото
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let {
                selectedMediaUri = it
                selectedMediaType = MediaType.PHOTO
            }
        }
    }
    
    // Запуск выбора существующего видео
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedMediaUri = it
            selectedMediaType = MediaType.VIDEO
        }
    }
    
    // Запуск камеры для создания видео
    val takeVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            tempVideoUri?.let {
                selectedMediaUri = it
                selectedMediaType = MediaType.VIDEO
            }
        }
    }
    
    // Запуск выбора существующего аудио
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedMediaUri = it
            selectedMediaType = MediaType.AUDIO
        }
    }
    
    // Функция для запуска камеры
    fun launchCamera() {
        try {
            val photoUri = createImageFile()
            if (photoUri != null) {
                tempPhotoUri = photoUri
                takePictureLauncher.launch(photoUri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Функция для запуска видеозаписи
    fun launchVideoRecorder() {
        try {
            val videoUri = createVideoFile()
            if (videoUri != null) {
                tempVideoUri = videoUri
                takeVideoLauncher.launch(videoUri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Запрос разрешений
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[android.Manifest.permission.RECORD_AUDIO] ?: false
        val storageGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions[android.Manifest.permission.READ_MEDIA_IMAGES] ?: false &&
            permissions[android.Manifest.permission.READ_MEDIA_VIDEO] ?: false &&
            permissions[android.Manifest.permission.READ_MEDIA_AUDIO] ?: false
        } else {
            permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false &&
            permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
        }
        
        // Если разрешения получены, запускаем соответствующее действие
        if (cameraGranted && !audioGranted) {
            launchCamera()
        } else if (cameraGranted && audioGranted) {
            launchVideoRecorder()
        }
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
                
                // Выбранный медиафайл (если есть)
                selectedMediaUri?.let { uri ->
                    when (selectedMediaType) {
                        MediaType.PHOTO -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Выбранное фото",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                
                                IconButton(
                                    onClick = {
                                        selectedMediaUri = null
                                        selectedMediaType = null
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(32.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Удалить фото",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        MediaType.VIDEO -> {
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
                                    Icon(
                                        imageVector = Icons.Default.VideoLibrary,
                                        contentDescription = "Видео",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Видео выбрано",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        selectedMediaUri = null
                                        selectedMediaType = null
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Удалить видео",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        MediaType.AUDIO -> {
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
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "Аудио",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Аудио выбрано",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        selectedMediaUri = null
                                        selectedMediaType = null
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Удалить аудио",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
                
                // Кнопки для выбора медиафайлов
                if (selectedMediaUri == null) {
                    if (showMediaOptions) {
                        // Расширенные опции для медиа
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Опции для фото
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { photoPickerLauncher.launch("image/*") }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Photo,
                                    contentDescription = "Выбрать фото из галереи",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Выбрать фото из галереи")
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { 
                                        permissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Сделать фото",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Сделать фото")
                            }
                            
                            // Опции для видео
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { videoPickerLauncher.launch("video/*") }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = "Выбрать видео из галереи",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Выбрать видео из галереи")
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { 
                                        permissionLauncher.launch(arrayOf(
                                            android.Manifest.permission.CAMERA,
                                            android.Manifest.permission.RECORD_AUDIO
                                        ))
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Записать видео",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Записать видео")
                            }
                            
                            // Опции для аудио
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { audioPickerLauncher.launch("audio/*") }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Выбрать аудио из галереи",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Выбрать аудио из галереи")
                            }
                            
                            // Кнопка для скрытия опций
                            TextButton(
                                onClick = { showMediaOptions = false },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Скрыть опции")
                            }
                        }
                    } else {
                        // Кнопка для показа опций
                        Button(
                            onClick = { showMediaOptions = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Добавить медиа"
                            )
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
                enabled = (text.isNotBlank() || selectedMediaUri != null)
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        }
    )
}

/**
 * Диалог для редактирования текстового комментария
 */
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
            TextButton(
                onClick = { onSave(text) },
                enabled = text.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

/**
 * Диалог для просмотра медиа-комментария
 */
@Composable
fun MediaPreviewDialog(
    comment: Comment,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    
    // Преобразуем строку URI в объект Uri
    val mediaUri = remember { android.net.Uri.parse(comment.mediaUri) }
    
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
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
                    Text(
                        text = comment.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                when (comment.mediaType) {
                    MediaType.PHOTO -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(mediaUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Фото",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    MediaType.VIDEO -> {
                        // Для видео используем Intent для открытия видеоплеера
                        Button(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        setDataAndType(mediaUri, "video/*")
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    // Можно добавить Toast с сообщением об ошибке
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "Воспроизвести видео"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Воспроизвести видео")
                        }
                    }
                    MediaType.AUDIO -> {
                        // Для аудио добавляем простой плеер
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isPlaying) {
                                        mediaPlayer?.pause()
                                        isPlaying = false
                                    } else {
                                        try {
                                            if (mediaPlayer == null) {
                                                mediaPlayer = android.media.MediaPlayer().apply {
                                                    setDataSource(context, mediaUri)
                                                    prepare()
                                                    setOnCompletionListener {
                                                        isPlaying = false
                                                    }
                                                }
                                            }
                                            mediaPlayer?.start()
                                            isPlaying = true
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            // Можно добавить Toast с сообщением об ошибке
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.MusicNote,
                                    contentDescription = if (isPlaying) "Остановить" else "Воспроизвести"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isPlaying) "Остановить" else "Воспроизвести")
                            }
                        }
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
} 