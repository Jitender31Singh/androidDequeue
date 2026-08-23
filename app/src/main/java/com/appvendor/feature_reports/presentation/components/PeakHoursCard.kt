package com.appvendor.feature_reports.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appvendor.feature_reports.domain.model.PeakHour

@Composable
fun PeakHoursCard(
    hours: List<PeakHour>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Peak Hours",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "When your shop is busiest",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (hours.isEmpty() || hours.all { it.orderCount == 0 }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141917))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No peak hour data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            return
        }

        var selectedIndex by remember { mutableStateOf<Int?>(null) }
        val maxOrders = hours.maxOf { it.orderCount }.coerceAtLeast(1)
        val peakIndex = hours.indexOfFirst { it.orderCount == maxOrders }

        val primaryColor = MaterialTheme.colorScheme.primary
        val defaultColor = Color.White.copy(alpha = 0.2f)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF141917))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // Tooltip area
            Box(modifier = Modifier.fillMaxWidth().height(24.dp), contentAlignment = Alignment.Center) {
                val currentIdx = selectedIndex ?: peakIndex
                if (currentIdx in hours.indices) {
                    val h = hours[currentIdx]
                    Text(
                        text = "${formatHour(h.hour)} · ${h.orderCount} orders",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .pointerInput(hours) {
                        detectTapGestures { offset ->
                            val barWidth = size.width / hours.size
                            val tappedIndex = (offset.x / barWidth).toInt().coerceIn(0, hours.lastIndex)
                            selectedIndex = tappedIndex
                        }
                    }
            ) {
                val barTotalWidth = size.width / hours.size
                val barWidth = barTotalWidth * 0.6f // 60% of available width
                
                hours.forEachIndexed { index, peak ->
                    val fraction = peak.orderCount.toFloat() / maxOrders
                    // Tallest gets a slight height boost to stand out more
                    val isPeak = index == peakIndex
                    val isSelected = index == selectedIndex

                    val heightAdjustment = if (isPeak) 1.05f else 1.0f
                    val actualHeight = (fraction * size.height * 0.9f * heightAdjustment).coerceAtLeast(4f)
                    
                    val xOffset = (index * barTotalWidth) + (barTotalWidth - barWidth) / 2f
                    val yOffset = size.height - actualHeight
                    
                    val color = if (isPeak || isSelected) primaryColor else defaultColor
                    
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(xOffset, yOffset),
                        size = Size(barWidth, actualHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis labels (Abbreviated, evenly spaced: e.g. 6AM, 9AM, 12PM, 3PM, 6PM, 9PM)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labelIndices = listOf(6, 9, 12, 15, 18, 21) // Or evenly spaced based on items
                // Let's just pick 6 evenly spaced items
                val step = (hours.size / 5).coerceAtLeast(1)
                for (i in 0 until hours.size step step) {
                    Text(
                        text = formatHour(hours[i].hour),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Insight text
            if (peakIndex in hours.indices && hours[peakIndex].orderCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                val peakHour = hours[peakIndex].hour
                Text(
                    text = "Peak time: ${formatHour(peakHour)}–${formatHour((peakHour + 1) % 24)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun formatHour(hour: Int): String {
    val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val amPm = if (hour < 12) "AM" else "PM"
    return "$h $amPm"
}
