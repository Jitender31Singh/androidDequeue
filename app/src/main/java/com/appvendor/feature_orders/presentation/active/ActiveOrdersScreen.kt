package com.appvendor.feature_orders.presentation.active

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appvendor.feature_orders.domain.model.OrderStatus
import com.appvendor.feature_orders.domain.model.OrderSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveOrdersScreen(
    viewModel: ActiveOrdersViewModel,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Orders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { viewModel.refresh() }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(min = 48.dp),
                placeholder = { Text("Search queue number...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(14.dp))

            // Filter Chips
            OrderFilterChips(
                currentFilter = state.filterStatus,
                orders = state.orders,
                allowedStatuses = state.orderVisibilityStatuses,
                onFilterSelected = { viewModel.updateFilterStatus(it) }
            )

            Spacer(Modifier.height(14.dp))

            // Orders List
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                }
            } else if (state.filteredOrders.isEmpty()) {
                EmptyOrderState(filter = state.filterStatus)
            } else {
                val sortedOrders = sortOrders(state.filteredOrders)
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedOrders, key = { it.id }) { order ->
                        val canSeePrice = !state.userRoles.contains("ROLE_VENDOR_KITCHEN")
                        OrderCard(
                            order = order,
                            userPermissions = state.userPermissions,
                            canSeePrice = canSeePrice,
                            onClick = { onOrderClick(order.id) },
                            onAccept = { viewModel.updateStatus(order.id, OrderStatus.ACCEPTED) },
                            onReject = { viewModel.updateStatus(order.id, OrderStatus.CANCELLED) },
                            onAdvanceStatus = {
                                order.status.nextStatus()?.let { next ->
                                    viewModel.updateStatus(order.id, next)
                                }
                            }
                        )
                    }
                }
            }
        }

        state.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(error)
            }
        }
    }
}

// Ensure proper sorting: Pending -> Accepted -> Preparing -> Ready -> Collected -> Cancelled
// Within each group, sort by time (newest first assuming createdAt can be compared string-wise, or just fallback to id)
fun sortOrders(orders: List<OrderSummary>): List<OrderSummary> {
    val statusOrder = listOf(
        OrderStatus.PENDING,
        OrderStatus.ACCEPTED,
        OrderStatus.PREPARING,
        OrderStatus.READY,
        OrderStatus.COMPLETED,
        OrderStatus.CANCELLED
    )
    return orders.sortedWith(compareBy({ statusOrder.indexOf(it.status) }, { it.createdAt.reversed() }))
}

@Composable
fun OrderFilterChips(
    currentFilter: OrderStatus?,
    orders: List<OrderSummary>,
    allowedStatuses: Set<String>,
    onFilterSelected: (OrderStatus?) -> Unit
) {
    val filters = listOf(
        null to "All",
        OrderStatus.PENDING to "Pending",
        OrderStatus.ACCEPTED to "Accepted",
        OrderStatus.PREPARING to "Preparing",
        OrderStatus.READY to "Ready",
        OrderStatus.COMPLETED to "Completed"
    ).filter { (status, _) ->
        if (status == null) true
        else {
            if (allowedStatuses.isEmpty()) true
            else {
                allowedStatuses.contains(status.name)
            }
        }
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (status, label) ->
            val count = if (status == null) orders.size else orders.count { it.status == status }
            val isSelected = currentFilter == status
            
            FilterChipItem(
                label = "$label $count",
                isSelected = isSelected,
                onClick = { onFilterSelected(status) }
            )
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
fun EmptyOrderState(filter: OrderStatus?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (filter == null) "No active orders" else "No ${filter.displayLabel().lowercase()} orders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "You're all caught up.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OrderCard(
    order: OrderSummary,
    userPermissions: Set<String>,
    canSeePrice: Boolean,
    onClick: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onAdvanceStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg) = getStatusColors(order.status)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left subtle accent line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                // Header: Queue Number, Badge, Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = order.queueNumber,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = order.status.displayLabel().uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }
                    Text(
                        text = com.appvendor.core.util.DateUtils.formatToIstReadableTime(order.createdAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Content: Items and Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${order.itemCount} ${if (order.itemCount == 1) "item" else "items"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (canSeePrice) {
                        Text(
                            text = "₹${order.totalAmount}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                val combinedFields = mutableMapOf<String, String>()
                order.metadata?.let { combinedFields.putAll(it) }
                order.customFields?.let { combinedFields.putAll(it) }

                if (combinedFields.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            combinedFields.forEach { (key, value) ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (order.status == OrderStatus.PENDING) {
                        if (userPermissions.contains("order.cancel")) {
                            OutlinedButton(
                                onClick = onReject,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
                            ) {
                                Text("Reject", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (userPermissions.contains("order.accept") && userPermissions.contains("order.cancel")) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        if (userPermissions.contains("order.accept")) {
                            Button(
                                onClick = onAccept,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Accept", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else if (order.status == OrderStatus.COMPLETED || order.status == OrderStatus.CANCELLED) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(order.status.displayLabel(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                        }
                    } else {
                        val actionLabel = order.status.actionLabel()
                        val canAdvance = when (order.status) {
                            OrderStatus.ACCEPTED -> userPermissions.contains("order.prepare")
                            OrderStatus.PREPARING -> userPermissions.contains("order.ready")
                            OrderStatus.READY -> userPermissions.contains("order.complete")
                            else -> false
                        }
                        if (actionLabel.isNotEmpty() && canAdvance) {
                            Button(
                                onClick = onAdvanceStatus,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = statusColor)
                            ) {
                                Text(actionLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getStatusColors(status: OrderStatus): Pair<Color, Color> {
    return when (status) {
        OrderStatus.PENDING -> Color(0xFFF57F17) to Color(0xFFFFFDE7)      // Amber
        OrderStatus.ACCEPTED -> Color(0xFF0288D1) to Color(0xFFE1F5FE)     // Blue/Teal
        OrderStatus.PREPARING -> Color(0xFF7B1FA2) to Color(0xFFF3E5F5)    // Purple
        OrderStatus.READY -> Color(0xFF388E3C) to Color(0xFFE8F5E9)        // Green
        OrderStatus.COMPLETED -> Color(0xFF616161) to Color(0xFFF5F5F5)    // Neutral
        OrderStatus.CANCELLED -> Color(0xFFD32F2F) to Color(0xFFFFEBEE)    // Red
    }
}
