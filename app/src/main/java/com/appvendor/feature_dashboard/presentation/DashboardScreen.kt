package com.appvendor.feature_dashboard.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appvendor.core.utils.TimeUtils
import com.appvendor.feature_dashboard.data.remote.dto.OrderSummary
import com.appvendor.feature_dashboard.data.remote.dto.TodayStats
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userName: String? = null,
    modifier: Modifier = Modifier,
    onViewAllOrdersClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.refreshOrders() },
        modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC))
    ) {
        val data = state.dashboardData
        if (data == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color(0xFFD97706))
                } else if (state.error != null) {
                    Text(text = state.error ?: "Error loading dashboard", color = MaterialTheme.colorScheme.error)
                } else {
                    Text("No Dashboard Data", color = Color(0xFF64748B))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                item {
                    HeaderSection(userName = userName)
                }

                item {
                    StatisticsGrid(stats = data.todayStats)
                }

                item {
                    LiveQueueSection(
                        currentlyServing = data.currentlyServing,
                        recentOrders = data.recentOrders,
                        onViewAllClick = onViewAllOrdersClick
                    )
                }

                item {
                    AverageWaitTimeCard(waitTime = data.averageWaitTime)
                }

                item {
                    val filteredOrders = if (state.orderVisibilityStatuses.isNotEmpty()) {
                        data.recentOrders.filter { state.orderVisibilityStatuses.contains(it.status.uppercase()) }
                    } else {
                        data.recentOrders
                    }
                    RecentOrdersSection(
                        orders = filteredOrders,
                        userRoles = state.userRoles,
                        onViewAllClick = onViewAllOrdersClick
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom nav
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(userName: String?) {
    val hour = java.time.LocalTime.now().hour
    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    val displayName = if (!userName.isNullOrBlank()) userName else "Chef"

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "CHAI CORNER",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$greeting, $displayName! 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
        }
    }
}

@Composable
private fun StatisticsGrid(stats: TodayStats) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatisticCard(
                label = "Total Orders",
                value = stats.totalOrders.toString(),
                icon = Icons.Default.ReceiptLong,
                modifier = Modifier.weight(1f)
            )
            StatisticCard(
                label = "Pending",
                value = stats.pendingOrders.toString(),
                icon = Icons.Default.PendingActions,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatisticCard(
                label = "Preparing",
                value = stats.preparingOrders.toString(),
                icon = Icons.Default.SoupKitchen,
                modifier = Modifier.weight(1f)
            )
            StatisticCard(
                label = "Ready",
                value = stats.readyOrders.toString(),
                icon = Icons.Default.TaskAlt,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatisticCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFF1F5F9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun LiveQueueSection(
    currentlyServing: String?,
    recentOrders: List<OrderSummary>,
    onViewAllClick: () -> Unit
) {
    val nextOrder = recentOrders.firstOrNull { it.status.equals("PENDING", ignoreCase = true) || it.status.equals("ACCEPTED", ignoreCase = true) }
    val preparingOrder = recentOrders.firstOrNull { it.status.equals("PREPARING", ignoreCase = true) }
    val servingOrder = recentOrders.firstOrNull { it.queueNumber == currentlyServing }

    Column {
        SectionHeader(title = "Live Queue", onActionClick = onViewAllClick)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QueuePipelineStep(
                    title = "Now Serving",
                    queueId = servingOrder?.queueNumber ?: currentlyServing ?: "--",
                    itemCount = servingOrder?.itemCount,
                    isHighlighted = true,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(20.dp).padding(horizontal = 4.dp)
                )
                
                QueuePipelineStep(
                    title = "Next",
                    queueId = nextOrder?.queueNumber ?: "--",
                    itemCount = nextOrder?.itemCount,
                    isHighlighted = false,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(20.dp).padding(horizontal = 4.dp)
                )
                
                QueuePipelineStep(
                    title = "Preparing",
                    queueId = preparingOrder?.queueNumber ?: "--",
                    itemCount = preparingOrder?.itemCount,
                    isHighlighted = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QueuePipelineStep(
    title: String,
    queueId: String,
    itemCount: Int?,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isHighlighted) Color(0xFFFFEDD5) else Color(0xFFF8FAFC))
                .border(1.dp, if (isHighlighted) Color(0xFFFFDBC7) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = queueId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isHighlighted) Color(0xFF9A3412) else Color(0xFF0F172A)
                )
                if (itemCount != null && queueId != "--") {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$itemCount items",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isHighlighted) Color(0xFFC2410C) else Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
private fun AverageWaitTimeCard(waitTime: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E7FF)), // Light indigo
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Wait Time",
                    tint = Color(0xFF4F46E5), // Primary Indigo
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Average Wait Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$waitTime min",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
private fun RecentOrdersSection(orders: List<OrderSummary>, userRoles: Set<String>, onViewAllClick: () -> Unit) {
    Column {
        SectionHeader(title = "Recent Orders", onActionClick = onViewAllClick)

        if (orders.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No recent orders", color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    orders.take(5).forEachIndexed { index, order ->
                        RecentOrderRow(order = order, userRoles = userRoles)
                        if (index < orders.take(5).size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

private data class OrderStatusUi(val icon: ImageVector, val iconColor: Color, val iconBg: Color, val chipBg: Color, val chipText: Color)

@Composable
private fun RecentOrderRow(order: OrderSummary, userRoles: Set<String>) {
    val ui = when (order.status.uppercase()) {
        "COMPLETED" -> OrderStatusUi(Icons.Default.CheckCircle, Color(0xFF475569), Color(0xFFF1F5F9), Color(0xFFF1F5F9), Color(0xFF475569))
        "READY" -> OrderStatusUi(Icons.Default.TaskAlt, Color(0xFF059669), Color(0xFFD1FAE5), Color(0xFFD1FAE5), Color(0xFF059669))
        "PREPARING" -> OrderStatusUi(Icons.Default.SoupKitchen, Color(0xFF4F46E5), Color(0xFFE0E7FF), Color(0xFFE0E7FF), Color(0xFF4F46E5))
        "ACCEPTED" -> OrderStatusUi(Icons.Default.ThumbUpAlt, Color(0xFF0284C7), Color(0xFFE0F2FE), Color(0xFFE0F2FE), Color(0xFF0284C7))
        "PENDING" -> OrderStatusUi(Icons.Default.Schedule, Color(0xFFD97706), Color(0xFFFEF3C7), Color(0xFFFEF3C7), Color(0xFFD97706))
        else -> OrderStatusUi(Icons.Default.Receipt, Color(0xFF64748B), Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Could navigate to detail */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(ui.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = ui.icon, contentDescription = null, tint = ui.iconColor, modifier = Modifier.size(24.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Order ${order.queueNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ui.chipBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = ui.chipText
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            val formattedPrice = String.format(Locale.US, "%.2f", order.totalAmount)
            val isKitchen = userRoles.contains("ROLE_VENDOR_KITCHEN")
            
            val priceString = if (isKitchen) "${order.itemCount} items" else "${order.itemCount} items · ₹$formattedPrice"
            
            Text(
                text = priceString,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = TimeUtils.getRelativeTime(order.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "View All →",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFD97706),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onActionClick).padding(4.dp)
        )
    }
}
