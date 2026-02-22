package com.sharai.chronotrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharai.chronotrack.ChronoTrackApp
import com.sharai.chronotrack.ui.components.PieChartView
import com.sharai.chronotrack.ui.utils.getIconByClassName
import com.sharai.chronotrack.ui.utils.ColorUtils
import com.sharai.chronotrack.viewmodel.ActivityStatistics
import com.sharai.chronotrack.viewmodel.DayStatistics
import com.sharai.chronotrack.viewmodel.StatisticsViewModel
import com.sharai.chronotrack.viewmodel.TimeEntryWithActivity
import com.sharai.chronotrack.viewmodel.CommentViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onTimeEntryClick: (Long) -> Unit
) {
    val selectedRange by viewModel.selectedRange.collectAsState()
    val timeEntries by viewModel.timeEntriesForSelectedRange.collectAsState(initial = emptyList())
    val activityStatistics by viewModel.activityStatistics.collectAsState(initial = emptyList())
    val firstActivityDate by viewModel.firstActivityDate.collectAsState()
    var showDateRangePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = selectedRange.startDate.toEpochDay() * 24 * 60 * 60 * 1000,
        initialSelectedEndDateMillis = selectedRange.endDate.toEpochDay() * 24 * 60 * 60 * 1000,
        yearRange = IntRange(firstActivityDate.year, LocalDate.now().year),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = LocalDate.ofEpochDay(utcTimeMillis / (24 * 60 * 60 * 1000))
                return !date.isBefore(firstActivityDate) && !date.isAfter(LocalDate.now())
            }
        }
    )

    val timePickerState = rememberTimePickerState()

    LaunchedEffect(dateRangePickerState.selectedEndDateMillis) {
        val startMillis = dateRangePickerState.selectedStartDateMillis
        val endMillis = dateRangePickerState.selectedEndDateMillis
        if (startMillis != null && endMillis != null) {
            val startDate = LocalDate.ofEpochDay(startMillis / (24 * 60 * 60 * 1000))
            val endDate = LocalDate.ofEpochDay(endMillis / (24 * 60 * 60 * 1000))
            viewModel.setDateTimeRange(
                selectedRange.copy(
                    startDate = startDate,
                    endDate = endDate
                )
            )
            showDateRangePicker = false
        }
    }

    if (showDateRangePicker) {
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = { },
            dismissButton = { }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text("Выберите период для отображения статистики активности") },
                showModeToggle = false,
                headline = { },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }

    if (showTimePicker) {
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setDateTimeRange(
                            selectedRange.copy(
                                startTime = LocalTime.of(timePickerState.hour, timePickerState.minute),
                                endTime = LocalTime.of(23, 59, 59)
                            )
                        )
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Подтверждение удаления") },
            text = { Text("Вы действительно хотите удалить все записи за выбранный период?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntriesInRange()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок и выбор периода
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Статистика",
                style = MaterialTheme.typography.headlineMedium
            )
            Row {
                IconButton(onClick = { showDateRangePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Выбрать период")
                }
                IconButton(
                    onClick = { showDeleteConfirmation = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить записи за период",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Выбранный период
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Даты
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newStartDate = selectedRange.startDate.minusDays(1)
                        val newEndDate = if (selectedRange.startDate == selectedRange.endDate) {
                            newStartDate
                        } else {
                            selectedRange.endDate.minusDays(1)
                        }
                        viewModel.setDateTimeRange(selectedRange.copy(
                            startDate = newStartDate,
                            endDate = newEndDate
                        ))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Предыдущий день"
                    )
                }

                Text(
                    text = when {
                        selectedRange.startDate == selectedRange.endDate -> {
                            selectedRange.startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
                        }
                        selectedRange.startDate.year == selectedRange.endDate.year -> {
                            "${selectedRange.startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))} - ${selectedRange.endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))}"
                        }
                        else -> {
                            "${selectedRange.startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))} - ${selectedRange.endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))}"
                        }
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(
                    onClick = {
                        val newStartDate = selectedRange.startDate.plusDays(1)
                        val newEndDate = if (selectedRange.startDate == selectedRange.endDate) {
                            newStartDate
                        } else {
                            selectedRange.endDate.plusDays(1)
                        }
                        viewModel.setDateTimeRange(selectedRange.copy(
                            startDate = newStartDate,
                            endDate = newEndDate
                        ))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Следующий день"
                    )
                }
            }

            // Показываем выбранное время только если выбран один день
            if (selectedRange.startDate == selectedRange.endDate) {
                var isStartTimePickerVisible by remember { mutableStateOf(false) }
                var isEndTimePickerVisible by remember { mutableStateOf(false) }
                
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedRange.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { isStartTimePickerVisible = true }
                    )
                    Text(
                        text = " - ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedRange.endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { isEndTimePickerVisible = true }
                    )
                }

                if (isStartTimePickerVisible) {
                    DatePickerDialog(
                        onDismissRequest = { isStartTimePickerVisible = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.setDateTimeRange(
                                        selectedRange.copy(
                                            startTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                        )
                                    )
                                    isStartTimePickerVisible = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { isStartTimePickerVisible = false }) {
                                Text("Отмена")
                            }
                        }
                    ) {
                        TimePicker(
                            state = timePickerState,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                if (isEndTimePickerVisible) {
                    DatePickerDialog(
                        onDismissRequest = { isEndTimePickerVisible = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.setDateTimeRange(
                                        selectedRange.copy(
                                            endTime = LocalTime.of(timePickerState.hour, timePickerState.minute, 59)
                                        )
                                    )
                                    isEndTimePickerVisible = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { isEndTimePickerVisible = false }) {
                                Text("Отмена")
                            }
                        }
                    ) {
                        TimePicker(
                            state = timePickerState,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activityStatistics.isEmpty() && timeEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет записей за выбранный период",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (activityStatistics.isNotEmpty()) {
                    item {
                        Text(
                            text = "Общая статистика",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    item {
                        PieChartView(
                            statistics = activityStatistics,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(vertical = 16.dp)
                        )
                    }

                    items(activityStatistics) { stat ->
                        ActivityStatisticsCard(stat)
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "Записи активностей",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                items(timeEntries) { dayStats ->
                    DayHeader(dayStats.date)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dayStats.entries.forEach { entry ->
                            TimeEntryListItem(entry, { showDeleteConfirmation = true }, { onTimeEntryClick(entry.timeEntry.id) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeEntryListItem(
    entry: TimeEntryWithActivity,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as ChronoTrackApp
    
    val commentViewModel = viewModel<CommentViewModel>(
        factory = CommentViewModel.Factory(app.commentRepository)
    )
    
    val hasComments by commentViewModel.hasComments(entry.timeEntry.id).collectAsState(initial = false)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onItemClick),
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
                                color = Color(entry.activity.color),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconByClassName(entry.activity.icon),
                            contentDescription = entry.activity.name,
                            tint = ColorUtils.getContrastColor(entry.activity.color),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = entry.activity.name,
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
                    text = entry.timeEntry.duration?.let { formatDuration(it) } ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Время начала и окончания
            Text(
                text = when {
                    entry.timeEntry.endTime == null -> {
                        "${entry.timeEntry.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - настоящее время"
                    }
                    entry.timeEntry.startTime.toLocalDate() == entry.timeEntry.endTime.toLocalDate() -> {
                        "${entry.timeEntry.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${entry.timeEntry.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                    }
                    else -> {
                        "${entry.timeEntry.startTime.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))} - ${entry.timeEntry.endTime.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))}"
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ActivityStatisticsCard(stat: ActivityStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Цветной индикатор активности с иконкой
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ColorUtils.getComposeColor(stat.activity.color), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconByClassName(stat.activity.icon),
                    contentDescription = stat.activity.name,
                    tint = ColorUtils.getContrastColor(stat.activity.color),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Информация об активности
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stat.activity.name.take(16) + if (stat.activity.name.length > 16) "..." else "",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = buildString {
                        append(formatDuration(stat.totalDuration))
                        append(" (")
                        append(String.format("%.1f", stat.percentage))
                        append("%, ")
                        append(stat.entriesCount)
                        append(" ")
                        append(when {
                            stat.entriesCount % 10 == 1 && stat.entriesCount % 100 != 11 -> "запись"
                            stat.entriesCount % 10 in 2..4 && stat.entriesCount % 100 !in 12..14 -> "записи"
                            else -> "записей"
                        })
                        append(")")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()
    return buildString {
        if (hours > 0) {
            append(hours)
            append(" ч ")
        }
        if (minutes > 0 || hours == 0L) {
            append(minutes)
            append(" мин")
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(0) }
    var selectedMinute by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Выберите время") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Часы
                NumberPicker(
                    value = selectedHour,
                    onValueChange = { selectedHour = it },
                    range = 0..23,
                    format = { "%02d".format(it) }
                )
                Text(" : ")
                // Минуты
                NumberPicker(
                    value = selectedMinute,
                    onValueChange = { selectedMinute = it },
                    range = 0..59,
                    format = { "%02d".format(it) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(selectedHour, selectedMinute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    format: (Int) -> String = { it.toString() }
) {
    Column {
        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) }
        ) {
            Text("▲")
        }
        Text(
            text = format(value),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) }
        ) {
            Text("▼")
        }
    }
}

@Composable
fun DayHeader(date: LocalDate) {
    Text(
        text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
} 