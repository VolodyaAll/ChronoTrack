package com.sharai.chronotrack.ui.utils

import androidx.compose.ui.graphics.Color

/**
 * Утилиты для работы с цветами активностей
 */
object ColorUtils {
    // Список предопределенных цветов с их названиями
    val predefinedColors = listOf(
        ColorInfo(0xFFFF4081.toInt(), "Розовый"),
        ColorInfo(0xFF3F51B5.toInt(), "Индиго"),
        ColorInfo(0xFF4CAF50.toInt(), "Зеленый"),
        ColorInfo(0xFFFF9800.toInt(), "Оранжевый"),
        ColorInfo(0xFF2196F3.toInt(), "Синий"),
        ColorInfo(0xFF9C27B0.toInt(), "Фиолетовый"),
        ColorInfo(0xFFFF5722.toInt(), "Глубокий оранжевый"),
        ColorInfo(0xFF607D8B.toInt(), "Серо-синий"),
        ColorInfo(0xFFE91E63.toInt(), "Ярко-розовый"),
        ColorInfo(0xFF673AB7.toInt(), "Темно-фиолетовый"),
        ColorInfo(0xFF009688.toInt(), "Бирюзовый"),
        ColorInfo(0xFFCDDC39.toInt(), "Лаймовый"),
        ColorInfo(0xFFFFC107.toInt(), "Янтарный"),
        ColorInfo(0xFF795548.toInt(), "Коричневый"),
        ColorInfo(0xFF9E9E9E.toInt(), "Серый"),
        ColorInfo(0xFF00BCD4.toInt(), "Голубой"),
        ColorInfo(0xFF8BC34A.toInt(), "Светло-зеленый"),
        ColorInfo(0xFFFFEB3B.toInt(), "Желтый"),
        ColorInfo(0xFF03A9F4.toInt(), "Светло-синий"),
        ColorInfo(0xFFE040FB.toInt(), "Пурпурный")
    )

    // Получить цвет по его значению
    fun getColorById(colorId: Int): ColorInfo? {
        return predefinedColors.find { it.colorInt == colorId }
    }

    // Получить название цвета по его значению
    fun getColorName(colorId: Int): String {
        return getColorById(colorId)?.name ?: "Неизвестный"
    }

    // Получить Compose Color из Int значения
    fun getComposeColor(colorId: Int): Color {
        return Color(colorId)
    }

    // Проверить, используется ли цвет в списке активностей
    fun isColorUsed(colorId: Int, activeActivities: List<com.sharai.chronotrack.data.model.Activity>, currentActivityId: Long? = null): Boolean {
        return activeActivities
            .filter { it.id != currentActivityId }
            .any { it.color == colorId }
    }

    // Получить контрастный цвет для текста на фоне данного цвета
    fun getContrastColor(colorId: Int): Color {
        // Простой алгоритм для определения, должен ли текст быть темным или светлым
        // на основе яркости фона
        val red = (colorId shr 16) and 0xFF
        val green = (colorId shr 8) and 0xFF
        val blue = colorId and 0xFF
        
        // Формула для определения яркости (из W3C рекомендаций)
        val brightness = (red * 299 + green * 587 + blue * 114) / 1000
        
        // Если яркость > 125, используем темный текст, иначе - светлый
        return if (brightness > 125) Color.Black else Color.White
    }

    // Рекомендовать цвет, который еще не используется
    fun recommendColor(activeActivities: List<com.sharai.chronotrack.data.model.Activity>): Int {
        val usedColors = activeActivities.map { it.color }.toSet()
        return predefinedColors
            .firstOrNull { !usedColors.contains(it.colorInt) }?.colorInt
            ?: predefinedColors.first().colorInt
    }
}

// Класс для хранения информации о цвете
data class ColorInfo(
    val colorInt: Int,
    val name: String
) 