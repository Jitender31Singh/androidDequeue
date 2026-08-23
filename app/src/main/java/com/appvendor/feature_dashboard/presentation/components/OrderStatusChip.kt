package com.appvendor.feature_dashboard.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appvendor.feature_dashboard.domain.model.OrderStatus

@Composable
fun OrderStatusChip(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (status) {
            OrderStatus.PENDING -> Color(0xFFFFF9C4) // Yellow 100
            OrderStatus.CONFIRMED -> Color(0xFFE1F5FE) // Light Blue 100
            OrderStatus.PREPARING -> Color(0xFFFFF3E0) // Orange 100
            OrderStatus.READY -> Color(0xFFE8F5E9) // Green 100
            OrderStatus.COMPLETED -> Color(0xFFF5F5F5) // Grey 100
            OrderStatus.CANCELLED -> Color(0xFFFFEBEE) // Red 100
        },
        label = "ChipBackgroundColor"
    )

    val textColor by animateColorAsState(
        targetValue = when (status) {
            OrderStatus.PENDING -> Color(0xFFF57F17)
            OrderStatus.CONFIRMED -> Color(0xFF0277BD)
            OrderStatus.PREPARING -> Color(0xFFE65100)
            OrderStatus.READY -> Color(0xFF2E7D32)
            OrderStatus.COMPLETED -> Color(0xFF424242)
            OrderStatus.CANCELLED -> Color(0xFFC62828)
        },
        label = "ChipTextColor"
    )

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
