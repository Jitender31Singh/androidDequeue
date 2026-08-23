package com.appvendor.feature_orders.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appvendor.core.ui.components.OrderStatusBadge
import com.appvendor.core.util.DateUtils
import com.appvendor.feature_orders.domain.model.Order
import com.appvendor.feature_orders.domain.model.OrderItem
import com.appvendor.feature_orders.domain.model.OrderStatus
import com.appvendor.feature_orders.presentation.util.InvoicePrinter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    viewModel: OrderDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.dismissMessages()
        }
    }

    LaunchedEffect(state.triggerAutoPrint) {
        if (state.triggerAutoPrint && state.order != null) {
            InvoicePrinter.printInvoice(
                context = context,
                order = state.order!!,
                shopName = state.vendorDetails?.shopName ?: state.shopName,
                address = state.vendorDetails?.address?.street ?: "",
                phone = state.vendorDetails?.phone ?: "",
                email = state.vendorDetails?.email ?: "",
                gstNumber = state.settingsData?.gstNumber ?: "",
                paperWidth = state.printerConfig?.paperWidth ?: "80mm",
                printerType = state.printerConfig?.printerType ?: "BROWSER"
            )
            viewModel.clearAutoPrintTrigger()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC), // Light slate background
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.order?.queueNumber?.let { "Order #$it" } ?: "Order Detail",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFFFFF)
                )
            )
        },
        bottomBar = {
            state.order?.let { order ->
                OrderBottomBar(
                    order = order,
                    isUpdating = state.isUpdating,
                    userPermissions = state.userPermissions,
                    userRoles = state.userRoles,
                    onAdvance = { viewModel.advanceStatus() },
                    onCancel = { viewModel.cancelOrder() },
                    onGenerateBill = {
                        viewModel.generateBill(order)
                        InvoicePrinter.printInvoice(
                            context = context,
                            order = order,
                            shopName = state.vendorDetails?.shopName ?: state.shopName,
                            address = state.vendorDetails?.address?.street ?: "",
                            phone = state.vendorDetails?.phone ?: "",
                            email = state.vendorDetails?.email ?: "",
                            gstNumber = state.settingsData?.gstNumber ?: "",
                            paperWidth = state.printerConfig?.paperWidth ?: "80mm",
                            printerType = state.printerConfig?.printerType ?: "BROWSER"
                        )
                    }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.order != null -> {
                    OrderDetailContent(order = state.order!!, userRoles = state.userRoles)
                }
                state.error != null -> {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
            }

            state.successMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor = Color(0xFF334155),
                    contentColor = Color.White
                ) {
                    Text(msg)
                }
            }
        }
    }
}

@Composable
private fun OrderDetailContent(order: Order, userRoles: Set<String>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Order Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        OrderStatusBadge(status = order.status)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Placed At",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = DateUtils.formatToIstReadableTime(order.createdAt),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        )
                    }
                    
                    val canSeePrice = !userRoles.contains("ROLE_VENDOR_KITCHEN")
                    
                    if (canSeePrice) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Grand Total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%.2f", order.totalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    if (!order.customerNote.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFFBEB))
                                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Default.EditNote,
                                    contentDescription = "Note",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Customer Note",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = order.customerNote,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    }

                    val combinedFields = mutableMapOf<String, String>()
                    order.metadata?.let { combinedFields.putAll(it) }
                    order.customFields?.let { combinedFields.putAll(it) }

                    if (combinedFields.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Additional Details",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                                
                                combinedFields.forEach { (key, value) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = value,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Items Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Items (${order.items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    val canSeePrice = !userRoles.contains("ROLE_VENDOR_KITCHEN")
                    order.items.forEachIndexed { index, item ->
                        OrderItemRow(item = item, canSeePrice = canSeePrice)
                        if (index < order.items.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFF1F5F9)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem, canSeePrice: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
            // Quantity Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.quantity}x",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = item.menuItemName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )
                
                if (item.selectedCustomizations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    val custString = item.selectedCustomizations.flatMap { it.selectedOptions }.joinToString(", ") { it.name }
                    Text(
                        text = "+ $custString",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = Color(0xFF64748B)
                    )
                }
                
                if (!item.specialInstructions.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "• ${item.specialInstructions}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = Color(0xFF64748B),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
        
        if (canSeePrice) {
            Text(
                text = "₹${String.format(Locale.US, "%.2f", item.totalPrice)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun OrderBottomBar(
    order: Order,
    isUpdating: Boolean,
    userPermissions: Set<String>,
    userRoles: Set<String>,
    onAdvance: () -> Unit,
    onCancel: () -> Unit,
    onGenerateBill: () -> Unit
) {
    val canSeeBill = !userRoles.contains("ROLE_VENDOR_KITCHEN") && 
                     (order.status == OrderStatus.READY || order.status == OrderStatus.COMPLETED)
    
    val primaryColor = Color(0xFF5E35B1) // Deep Violet
    
    Surface(
        color = Color(0xFFFFFFFF),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            when (order.status) {
                OrderStatus.PENDING -> {
                    if (userPermissions.contains("order.accept")) {
                        Button(
                            onClick = onAdvance,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !isUpdating,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("Accept Order", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (userPermissions.contains("order.cancel")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !isUpdating,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)) // Red
                        ) {
                            Text("Decline / Cancel", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                OrderStatus.ACCEPTED -> {
                    if (userPermissions.contains("order.prepare")) {
                        Button(
                            onClick = onAdvance,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !isUpdating,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("Start Preparing", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                OrderStatus.PREPARING -> {
                    if (userPermissions.contains("order.ready")) {
                        Button(
                            onClick = onAdvance,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !isUpdating,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("Mark as Ready", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                OrderStatus.READY -> {
                    if (userPermissions.contains("order.complete")) {
                        Button(
                            onClick = onAdvance,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !isUpdating,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("Mark Completed", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (canSeeBill) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onGenerateBill,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                        ) {
                            Text("Generate Bill", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                OrderStatus.COMPLETED -> {
                    if (canSeeBill) {
                        OutlinedButton(
                            onClick = onGenerateBill,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                        ) {
                            Text("Print Receipt / Generate Bill", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                OrderStatus.CANCELLED -> {
                    // No actions for cancelled orders
                }
            }
        }
    }
}
