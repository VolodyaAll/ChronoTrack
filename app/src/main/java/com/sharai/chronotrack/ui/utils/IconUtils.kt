package com.sharai.chronotrack.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

// Карта для быстрого поиска иконок по имени класса
private val iconMap = mapOf(
    // Базовые иконки
    "Icons.Default.Timer" to Icons.Default.Timer,
    "Icons.Default.Work" to Icons.Default.Work,
    "Icons.Default.Nightlight" to Icons.Default.Nightlight,
    "Icons.Default.Star" to Icons.Default.Star,
    "Icons.Default.Group" to Icons.Default.Group,
    "Icons.AutoMirrored.Filled.DirectionsRun" to Icons.AutoMirrored.Filled.DirectionsRun,
    "Icons.Default.Favorite" to Icons.Default.Favorite,
    "Icons.Default.Home" to Icons.Default.Home,
    "Icons.Default.Settings" to Icons.Default.Settings,
    "Icons.Default.Person" to Icons.Default.Person,
    
    // Дополнительные иконки для активностей
    "Icons.Default.Restaurant" to Icons.Default.Restaurant,        // Еда
    "Icons.Default.LocalCafe" to Icons.Default.LocalCafe,          // Кофе
    "Icons.Default.School" to Icons.Default.School,                // Учеба
    "Icons.Default.Book" to Icons.Default.Book,                    // Чтение
    "Icons.Default.Commute" to Icons.Default.Commute,              // Дорога
    "Icons.Default.DirectionsCar" to Icons.Default.DirectionsCar,  // Вождение
    "Icons.Default.Pets" to Icons.Default.Pets,                    // Домашние животные
    "Icons.Default.ShoppingCart" to Icons.Default.ShoppingCart,    // Покупки
    "Icons.Default.LocalHospital" to Icons.Default.LocalHospital,  // Здоровье
    "Icons.Default.Spa" to Icons.Default.Spa,                      // Отдых
    "Icons.Default.FitnessCenter" to Icons.Default.FitnessCenter,  // Фитнес
    "Icons.Default.LocalMovies" to Icons.Default.LocalMovies,      // Кино
    "Icons.Default.Tv" to Icons.Default.Tv,                        // ТВ
    "Icons.Default.Computer" to Icons.Default.Computer,            // Компьютер
    "Icons.Default.Smartphone" to Icons.Default.Smartphone,        // Телефон
    "Icons.Default.Brush" to Icons.Default.Brush,                  // Рисование
    "Icons.Default.MusicNote" to Icons.Default.MusicNote,          // Музыка
    "Icons.Default.Videogame" to Icons.Default.VideogameAsset,     // Видеоигры
    "Icons.Default.Call" to Icons.Default.Call,                    // Звонки
    "Icons.Default.Mail" to Icons.Default.Mail,                    // Почта
    "Icons.Default.Chat" to Icons.Default.Chat,                    // Общение
    "Icons.Default.Celebration" to Icons.Default.Celebration,      // Праздник
    "Icons.Default.EmojiEvents" to Icons.Default.EmojiEvents,      // Достижения
    "Icons.Default.Hiking" to Icons.Default.Hiking,                // Походы
    "Icons.Default.Park" to Icons.Default.Park,                    // Природа
    "Icons.Default.BeachAccess" to Icons.Default.BeachAccess,      // Пляж
    "Icons.Default.Flight" to Icons.Default.Flight,                // Путешествия
    "Icons.Default.LocalDining" to Icons.Default.LocalDining,      // Ресторан
    "Icons.Default.LocalBar" to Icons.Default.LocalBar,            // Бар
    "Icons.Default.Cleaning" to Icons.Default.CleaningServices,    // Уборка
    
    // Служебные иконки
    "Icons.Default.Add" to Icons.Default.Add,
    "Icons.Default.Delete" to Icons.Default.Delete,
    "Icons.Default.Edit" to Icons.Default.Edit,
    "Icons.Default.Check" to Icons.Default.Check,
    "Icons.Default.Close" to Icons.Default.Close,
    "Icons.Default.Search" to Icons.Default.Search,
    "Icons.Default.Menu" to Icons.Default.Menu,
    "Icons.Default.MoreVert" to Icons.Default.MoreVert,
    "Icons.Default.Refresh" to Icons.Default.Refresh,
    "Icons.Default.Info" to Icons.Default.Info,
    "Icons.Default.Warning" to Icons.Default.Warning,
    "Icons.Default.Error" to Icons.Default.Error,
    "Icons.Default.Notifications" to Icons.Default.Notifications,
    "Icons.Default.AccountCircle" to Icons.Default.AccountCircle,
    "Icons.Default.CalendarToday" to Icons.Default.CalendarToday
)

// Список доступных иконок для выбора в UI, формируется из iconMap
val availableIcons = iconMap.values.toList()

// Обратная карта для получения имени класса по иконке
private val reverseIconMap = iconMap.entries.associateBy({ it.value }, { it.key })

// Функция для получения иконки по полному имени класса
fun getIconByClassName(className: String): ImageVector {
    return iconMap[className] ?: Icons.Default.Timer // Значение по умолчанию
}

// Функция для получения полного имени класса иконки
fun getIconClassName(icon: ImageVector): String {
    return reverseIconMap[icon] ?: "Icons.Default.Timer" // Значение по умолчанию
} 