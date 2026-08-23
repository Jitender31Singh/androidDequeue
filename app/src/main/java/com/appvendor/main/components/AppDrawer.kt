package com.appvendor.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvendor.core.navigation.Routes

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun AppDrawer(
    shopName: String?,
    userEmail: String?,
    logoUrl: String?,
    currentRoute: String,
    userPermissions: Set<String>,
    userRoles: Set<String> = emptySet(),
    onNavigateToRoute: (String) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp) // Subtle border gap
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!logoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = "Shop Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = shopName?.firstOrNull()?.toString()?.uppercase() ?: "S",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = shopName ?: "Vendor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = userEmail ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (userRoles.contains("ROLE_VENDOR_ADMIN") || userRoles.contains("ROLE_VENDOR_MANAGER")) {
            com.appvendor.main.components.CardNavigationBar(
                currentRoute = currentRoute,
                userPermissions = userPermissions,
                onNavigate = onNavigateToRoute,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // Navigation Items
            if (userPermissions.contains("order.view")) {
                DrawerItem(
                    icon = Icons.Outlined.ReceiptLong,
                    label = "Orders",
                    isSelected = currentRoute == Routes.Orders::class.qualifiedName,
                    onClick = { onNavigateToRoute(Routes.Orders::class.qualifiedName ?: "") }
                )
            }
            if (userPermissions.contains("menu.view")) {
                DrawerItem(
                    icon = Icons.Outlined.Category,
                    label = "Categories",
                    isSelected = currentRoute == Routes.Categories::class.qualifiedName,
                    onClick = { onNavigateToRoute(Routes.Categories::class.qualifiedName ?: "") }
                )
                DrawerItem(
                    icon = Icons.Outlined.Tune,
                    label = "Customizations",
                    isSelected = currentRoute == Routes.Customizations::class.qualifiedName,
                    onClick = { onNavigateToRoute(Routes.Customizations::class.qualifiedName ?: "") }
                )
                DrawerItem(
                    icon = Icons.Outlined.RestaurantMenu,
                    label = "Menu Items",
                    isSelected = currentRoute == Routes.MenuItems::class.qualifiedName,
                    onClick = { onNavigateToRoute(Routes.MenuItems::class.qualifiedName ?: "") }
                )
            }
            if (userPermissions.contains("staff.view")) {
                DrawerItem(
                    icon = Icons.Outlined.Business,
                    label = "Departments",
                    isSelected = currentRoute == Routes.Departments::class.qualifiedName,
                    onClick = { onNavigateToRoute(Routes.Departments::class.qualifiedName ?: "") }
                )
                DrawerItem(
                    icon = Icons.Outlined.People,
                    label = "Staff",
                    isSelected = currentRoute == Routes.Staff::class.qualifiedName,
                    onClick = { onNavigateToRoute(Routes.Staff::class.qualifiedName ?: "") }
                )
            }
            if (userPermissions.contains("report.view")) {
                DrawerItem(
                    icon = Icons.Outlined.BarChart,
                    label = "Reports",
                    isSelected = currentRoute == Routes.Reports::class.qualifiedName,
                    onClick = { onNavigateToRoute(Routes.Reports::class.qualifiedName ?: "") }
                )
            }
            if (userPermissions.contains("menu.view")) {
                DrawerItem(
                    icon = Icons.Outlined.LocationOn,
                    label = "Geofence",
                    isSelected = currentRoute == Routes.Geofence::class.qualifiedName,
                    onClick = { onNavigateToRoute(Routes.Geofence::class.qualifiedName ?: "") }
                )
            }
            if (userPermissions.contains("qr.view")) {
                DrawerItem(
                    icon = Icons.Outlined.QrCode2,
                    label = "QR Code",
                    isSelected = currentRoute == Routes.QrCode::class.qualifiedName,
                    onClick = { onNavigateToRoute(Routes.QrCode::class.qualifiedName ?: "") }
                )
            }
        }
        
        Divider(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            thickness = 1.dp
        )
        
        // Logout
        DrawerItem(
            icon = Icons.Outlined.Logout,
            label = "Logout",
            isSelected = false,
            onClick = onLogoutClick
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                letterSpacing = 0.1.sp
            ),
            color = contentColor
        )
    }
}

@Preview
@Composable
private fun AppDrawerPreview() {
    MaterialTheme {
        AppDrawer(
            shopName = "My Awesome Shop",
            userEmail = "vendor@shop.com",
            logoUrl = null,
            currentRoute = Routes.Dashboard::class.qualifiedName ?: "",
            userPermissions = emptySet(),
            userRoles = emptySet(),
            onNavigateToRoute = {},
            onLogoutClick = {}
        )
    }
}
