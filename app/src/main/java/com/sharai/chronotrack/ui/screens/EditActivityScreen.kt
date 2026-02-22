package com.sharai.chronotrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sharai.chronotrack.data.model.Activity
import com.sharai.chronotrack.ui.utils.availableIcons
import com.sharai.chronotrack.ui.utils.getIconByClassName
import com.sharai.chronotrack.ui.utils.getIconClassName
import com.sharai.chronotrack.ui.utils.ColorUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityScreen(
    activity: Activity? = null,
    activeActivities: List<Activity> = emptyList(),
    archivedActivities: List<Activity> = emptyList(),
    onSave: (String, Int, String) -> Unit,
    onDelete: (() -> Unit)? = null,
    onCancel: () -> Unit,
    onPermanentDelete: ((Long) -> Unit)? = null,
    onRestoreActivity: ((Long) -> Unit)? = null
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFFFF4081.toInt()) }
    var selectedIcon by remember { mutableStateOf("Icons.Default.Timer") }
    var showArchiveConfirmation by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showColorInfo by remember { mutableStateOf(false) }
    var showIconInfo by remember { mutableStateOf(false) }
    var showArchivedActivities by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(activity) {
        if (activity != null) {
            name = activity.name
            selectedColor = activity.color
            selectedIcon = activity.icon
        } else {
            // Для новой активности рекомендуем цвет, который еще не используется
            selectedColor = ColorUtils.recommendColor(activeActivities)
        }
    }

    if (showArchiveConfirmation && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirmation = false },
            icon = { Icon(Icons.Default.VisibilityOff, contentDescription = null) },
            title = { Text("Скрыть активность") },
            text = { Text("Вы действительно хотите скрыть активность \"${activity?.name}\"? Вы сможете восстановить её позже из архива.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showArchiveConfirmation = false
                        onDelete()
                    }
                ) {
                    Text("Скрыть")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveConfirmation = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showDeleteConfirmation && activity != null && onPermanentDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Удаление активности") },
            text = { 
                Text(
                    "Вы действительно хотите полностью удалить активность \"${activity.name}\"?\n\n" +
                    "ВНИМАНИЕ: Это действие нельзя отменить. Все записи времени для этой активности также будут удалены."
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onPermanentDelete(activity.id)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить навсегда")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showColorInfo) {
        AlertDialog(
            onDismissRequest = { showColorInfo = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text("Выбор цвета") },
            text = { 
                Text(
                    "Рекомендуется выбирать разные цвета для активностей, чтобы они хорошо различались на диаграммах статистики.\n\n" +
                    "Цвета, отмеченные зеленой галочкой, уже используются в других активностях."
                ) 
            },
            confirmButton = {
                TextButton(onClick = { showColorInfo = false }) {
                    Text("Понятно")
                }
            }
        )
    }

    if (showIconInfo) {
        AlertDialog(
            onDismissRequest = { showIconInfo = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text("Выбор иконки") },
            text = { 
                Text(
                    "Выберите иконку, которая лучше всего отражает суть вашей активности.\n\n" +
                    "Иконки помогают быстро идентифицировать активности в списке и на диаграммах."
                ) 
            },
            confirmButton = {
                TextButton(onClick = { showIconInfo = false }) {
                    Text("Понятно")
                }
            }
        )
    }

    if (showArchivedActivities && archivedActivities.isNotEmpty() && onRestoreActivity != null) {
        AlertDialog(
            onDismissRequest = { showArchivedActivities = false },
            icon = { Icon(Icons.Default.Restore, contentDescription = null) },
            title = { Text("Архивированные активности") },
            text = { 
                Column {
                    Text(
                        "Выберите активность для восстановления:",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .fillMaxWidth()
                    ) {
                        items(archivedActivities) { archivedActivity ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onRestoreActivity(archivedActivity.id)
                                        showArchivedActivities = false
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(ColorUtils.getComposeColor(archivedActivity.color)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconByClassName(archivedActivity.icon),
                                            contentDescription = archivedActivity.name,
                                            tint = ColorUtils.getContrastColor(archivedActivity.color)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = archivedActivity.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showArchivedActivities = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (activity != null) "Редактирование активности" else "Новая активность"
                    )
                },
                actions = {
                    if (activity != null) {
                        // Кнопка скрытия активности
                        IconButton(
                            onClick = { showArchiveConfirmation = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Скрыть активность"
                            )
                        }
                        
                        // Кнопка полного удаления активности
                        IconButton(
                            onClick = { showDeleteConfirmation = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить активность",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else if (archivedActivities.isNotEmpty() && onRestoreActivity != null) {
                        // Кнопка восстановления из архива (только при создании новой активности)
                        IconButton(
                            onClick = { showArchivedActivities = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Восстановить из архива"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отмена")
                    }
                    Button(
                        onClick = { onSave(name, selectedColor, selectedIcon) },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Если есть архивированные активности и мы создаем новую активность, показываем кнопку
            if (activity == null && archivedActivities.isNotEmpty() && onRestoreActivity != null) {
                Button(
                    onClick = { showArchivedActivities = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Восстановить из архива (${archivedActivities.size})")
                    }
                }
            }

            // Название активности
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название активности") },
                modifier = Modifier.fillMaxWidth()
            )

            // Выбор цвета
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Цвет активности", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { showColorInfo = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Информация о выборе цвета"
                    )
                }
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(120.dp)
            ) {
                items(ColorUtils.predefinedColors) { colorInfo ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ColorUtils.getComposeColor(colorInfo.colorInt))
                            .clickable { selectedColor = colorInfo.colorInt }
                            .then(
                                if (colorInfo.colorInt == selectedColor) {
                                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Показываем галочку, если цвет уже используется
                        if (ColorUtils.isColorUsed(colorInfo.colorInt, activeActivities, activity?.id)) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Цвет уже используется",
                                tint = Color.Green
                            )
                        }
                    }
                }
            }

            // Выбор иконки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Иконка активности", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { showIconInfo = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Информация о выборе иконки"
                    )
                }
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(availableIcons) { icon ->
                    val iconClassName = getIconClassName(icon)
                    IconButton(
                        onClick = { selectedIcon = iconClassName },
                        modifier = Modifier
                            .size(48.dp)
                            .then(
                                if (iconClassName == selectedIcon) {
                                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                } else Modifier
                            )
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = iconClassName,
                            tint = if (iconClassName == selectedIcon) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
} 