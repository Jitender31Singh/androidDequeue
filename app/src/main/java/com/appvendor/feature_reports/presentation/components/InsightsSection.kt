package com.appvendor.feature_reports.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appvendor.feature_reports.domain.model.SummaryReport

@Composable
fun InsightsSection(
    report: SummaryReport,
    modifier: Modifier = Modifier
) {
    val insights = mutableListOf<String>()

    // Insight 1: Peak hour
    val maxPeak = report.peakHourReport.maxByOrNull { it.orderCount }
    if (maxPeak != null && maxPeak.orderCount > 0) {
        val peakHour = maxPeak.hour
        val amPm = if (peakHour < 12) "AM" else "PM"
        val h = if (peakHour == 0) 12 else if (peakHour > 12) peakHour - 12 else peakHour
        insights.add("Most orders arrive around $h $amPm.")
    }

    // Insight 2: Top Item
    val topItem = report.popularItemReport.maxByOrNull { it.totalRevenue }
    if (topItem != null && topItem.totalRevenue > 0) {
        insights.add("${topItem.menuItemName} generated the most revenue.")
    }

    // Insight 3: Queue Efficiency
    if (report.queueStatsReport.totalServed > 10) {
        if (report.queueStatsReport.averageWaitTime < 5) {
            insights.add("Queue wait times are excellent (< 5m).")
        } else if (report.queueStatsReport.averageWaitTime > 15) {
            insights.add("Queue wait times are long (> 15m).")
        }
    }

    if (insights.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Insights",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF141917))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            insights.forEach { insight ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
