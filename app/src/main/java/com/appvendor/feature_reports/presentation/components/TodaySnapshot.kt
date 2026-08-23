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
import com.appvendor.feature_reports.domain.model.TodayReport

@Composable
fun TodaySnapshot(report: TodayReport, modifier: Modifier = Modifier) {
    val avgOrder = if (report.totalOrders > 0) report.totalRevenue / report.totalOrders else 0.0

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Today",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCell(
                label = "Revenue",
                value = "₹${"%.2f".format(report.totalRevenue)}",
                trend = report.comparedToYesterday.revenue,
                isPrimary = true,
                modifier = Modifier.weight(1f)
            )
            MetricCell(
                label = "Orders",
                value = "${report.totalOrders}",
                trend = report.comparedToYesterday.orders,
                isPrimary = false,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCell(
                label = "Completed",
                value = "${report.completedOrders}",
                trend = null,
                isPrimary = false,
                modifier = Modifier.weight(1f)
            )
            MetricCell(
                label = "Avg. Order",
                value = "₹${"%.2f".format(avgOrder)}",
                trend = null,
                isPrimary = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCell(
    label: String,
    value: String,
    trend: Double?,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaceColor = Color(0xFF141917)
    val borderColor = Color.White.copy(alpha = 0.1f)
    val valueColor = if (isPrimary) MaterialTheme.colorScheme.primary else Color.White

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
        if (trend != null) {
            Spacer(modifier = Modifier.height(4.dp))
            val trendColor = if (trend >= 0) Color(0xFF388E3C) else Color(0xFFD32F2F)
            val trendSign = if (trend >= 0) "+" else ""
            Text(
                text = "$trendSign${"%.1f".format(trend)}%",
                style = MaterialTheme.typography.labelSmall,
                color = trendColor
            )
        }
    }
}
