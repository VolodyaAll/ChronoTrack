package com.sharai.chronotrack.ui.components

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.sharai.chronotrack.viewmodel.ActivityStatistics
import java.time.Duration

@Composable
fun PieChartView(
    statistics: List<ActivityStatistics>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PieChart(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(android.graphics.Color.TRANSPARENT)
                holeRadius = 58f
                transparentCircleRadius = 61f
                setDrawCenterText(true)
                setCenterTextSize(12f)
                setCenterTextColor(android.graphics.Color.WHITE)
                setUsePercentValues(true)
                legend.isEnabled = false
                
                // Настройка выносных подписей
                setDrawEntryLabels(true)
                setEntryLabelColor(android.graphics.Color.WHITE)
                setEntryLabelTextSize(12f)
                
                // Увеличиваем отступ для выносных подписей
                extraLeftOffset = 30f
                extraRightOffset = 30f
                extraTopOffset = 30f
                extraBottomOffset = 30f
            }
        },
        update = { chart ->
            // Разделяем статистику на значимые и малые сегменты
            val significantStats = statistics.filter { it.percentage >= 2f }
            val smallStats = statistics.filter { it.percentage < 2f }
            
            // Вычисляем общее время
            val totalDuration = Duration.ofSeconds(
                statistics.sumOf { it.totalDuration.seconds }
            )
            val days = totalDuration.toDays()
            val hours = totalDuration.toHoursPart()
            val minutes = totalDuration.toMinutesPart()
            
            // Формируем текст общего времени
            val totalTimeText = buildString {
                if (days > 0) {
                    append(days)
                    append(" д\n")
                }
                if (hours > 0 || days > 0) {
                    append(hours)
                    append(" ч\n")
                }
                append(minutes)
                append(" мин")
            }
            chart.centerText = totalTimeText
            
            // Создаем записи для диаграммы
            val entries = mutableListOf<PieEntry>()
            val colors = mutableListOf<Int>()
            
            // Добавляем значимые сегменты
            significantStats.forEach { stat ->
                entries.add(PieEntry(stat.percentage, stat.activity.name))
                colors.add(stat.activity.color)
            }
            
            // Добавляем объединенный сегмент для малых значений, если они есть
            val totalSmallPercentage = smallStats.sumOf { it.percentage.toDouble() }.toFloat()
            if (totalSmallPercentage > 0) {
                entries.add(PieEntry(totalSmallPercentage, ""))
                colors.add(android.graphics.Color.WHITE)
            }

            val dataSet = PieDataSet(entries, "").apply {
                setColors(colors.toIntArray(), 255)
                // Отключаем отображение процентов
                setDrawValues(false)
                
                // Настройка выносных линий
                valueLinePart1Length = 0.6f
                valueLinePart2Length = 0.6f
                valueLineWidth = 2f
                valueLineColor = android.graphics.Color.WHITE
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                
                // Настройка выносок
                setDrawIcons(false)
                isUsingSliceColorAsValueLineColor = true
            }

            val data = PieData(dataSet)
            chart.data = data
            chart.invalidate()
        }
    )
} 