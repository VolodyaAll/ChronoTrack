package com.sharai.chronotrack.data.database

import com.sharai.chronotrack.data.model.Activity

object PresetActivities {
    val activities = listOf(
        Activity(
            name = "Работа",
            color = 0xFFFF4081.toInt(), // Розовый
            icon = "Icons.Default.Work"
        ),
        Activity(
            name = "Сон",
            color = 0xFF3F51B5.toInt(), // Индиго
            icon = "Icons.Default.Nightlight"
        ),
        Activity(
            name = "Хобби",
            color = 0xFF4CAF50.toInt(), // Зеленый
            icon = "Icons.Default.Star"
        ),
        Activity(
            name = "Семья",
            color = 0xFFFF9800.toInt(), // Оранжевый
            icon = "Icons.Default.Group"
        ),
        Activity(
            name = "Спорт",
            color = 0xFF2196F3.toInt(), // Синий
            icon = "Icons.AutoMirrored.Filled.DirectionsRun"
        )
    )
} 