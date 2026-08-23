package com.appvendor.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvendor.feature_orders.domain.model.OrderStatus

@Composable
fun OrderStatusBadge(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.PENDING -> Color(0xFFF1F5F9) to Color(0xFF64748B) // Light Silver
        OrderStatus.ACCEPTED -> Color(0xFFE2E8F0) to Color(0xFF0F172A) // Silver to Navy
        OrderStatus.PREPARING -> Color(0xFFFFEDD5) to Color(0xFFC2410C) // Orange Tint
        OrderStatus.READY -> Color(0xFFFF6B00) to Color(0xFFFFFFFF) // Primary Orange Solid
        OrderStatus.COMPLETED -> Color(0xFFF8FAFC) to Color(0xFF94A3B8) // Muted Silver
        OrderStatus.CANCELLED -> Color(0xFFFEF2F2) to Color(0xFF991B1B) // Red Tint
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.displayLabel().uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                fontSize = 11.sp
            ),
            color = textColor
        )
    }
}
