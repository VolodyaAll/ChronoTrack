package com.sharai.chronotrack.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sharai.chronotrack.data.model.Activity
import com.sharai.chronotrack.ui.utils.getIconByClassName
import com.sharai.chronotrack.ui.utils.ColorUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

@Composable
fun ActivityTile(
    activity: Activity,
    isActive: Boolean,
    currentActivityStartTime: LocalDateTime?,
    onActivityClick: () -> Unit,
    onActivityStartTimeChange: (LocalDateTime) -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimeSlider by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(1f) }
    
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseAnim.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    if (showTimeSlider && currentActivityStartTime != null) {
        Dialog(onDismissRequest = { showTimeSlider = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Установить время начала активности",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val now = LocalDateTime.now()
                    val secondsSinceStart = currentActivityStartTime.until(now, java.time.temporal.ChronoUnit.SECONDS)
                    val selectedTime = now.minusSeconds((secondsSinceStart * (1 - sliderPosition)).roundToLong())
                    
                    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                    
                    Text(
                        "Время начала:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        selectedTime.format(dateTimeFormatter),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Кнопки быстрого выбора времени
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { 
                                // Устанавливаем время начала в начало предыдущей активности
                                sliderPosition = 0f 
                            }
                        ) {
                            Text(
                                text = "<<",
                                fontSize = 18.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.semantics {
                                    contentDescription = "Установить время начала предыдущей активности"
                                }
                            )
                        }
                        
                        Text(
                            text = "Выберите время",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        IconButton(
                            onClick = { 
                                // Устанавливаем время начала в текущее время
                                sliderPosition = 1f 
                            }
                        ) {
                            Text(
                                text = ">>",
                                fontSize = 18.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.semantics {
                                    contentDescription = "Установить текущее время"
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = currentActivityStartTime.format(dateTimeFormatter),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = now.format(dateTimeFormatter),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimeSlider = false }) {
                            Text("Отмена")
                        }
                        TextButton(
                            onClick = {
                                onActivityStartTimeChange(selectedTime)
                                showTimeSlider = false
                            }
                        ) {
                            Text("Применить")
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorUtils.getComposeColor(activity.color).copy(alpha = if (isActive) pulseAlpha else 0.8f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onActivityClick() },
                    onLongPress = { 
                        if (currentActivityStartTime != null && !isActive) {
                            sliderPosition = 1f
                            showTimeSlider = true
                        }
                    }
                )
            }
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = getIconByClassName(activity.icon),
                    contentDescription = activity.name,
                    tint = ColorUtils.getContrastColor(activity.color),
                    modifier = Modifier.size(32.dp)
                )
                
                if (isActive) {
                    val dotAnim = rememberInfiniteTransition(label = "dot")
                    val dotAlpha by dotAnim.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_alpha"
                    )
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Green.copy(alpha = dotAlpha))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = activity.name,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Редактировать",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
} 