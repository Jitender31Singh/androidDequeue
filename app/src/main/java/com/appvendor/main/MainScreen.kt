package com.appvendor.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appvendor.core.navigation.Routes
import com.appvendor.main.components.AppDrawer
import com.appvendor.main.components.AppTopBar
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    mainNavController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val nestedNavController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Dashboard::class.qualifiedName ?: ""

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !state.userRoles.contains("ROLE_VENDOR_KITCHEN") && !state.userRoles.contains("ROLE_VENDOR_COUNTER"),
        drawerContent = {
            AppDrawer(
                shopName = state.shopName,
                userEmail = state.userEmail,
                logoUrl = state.logoUrl,
                currentRoute = currentRoute,
                userPermissions = state.userPermissions,
                userRoles = state.userRoles,
                onNavigateToRoute = { route ->
                    scope.launch { drawerState.close() }
                    nestedNavController.navigate(route) {
                        popUpTo(Routes.Dashboard::class.qualifiedName ?: "") {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    viewModel.logout()
                    mainNavController.navigate(Routes.SignIn::class.qualifiedName ?: "sign_in") {
                        popUpTo(0)
                    }
                }
            )
        },
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    shopName = state.shopName,
                    isBusinessActive = state.isBusinessActive,
                    userRoles = state.userRoles,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onToggleActive = viewModel::toggleBusinessActive,
                    onLogoutClick = {
                        viewModel.logout()
                        mainNavController.navigate(Routes.SignIn::class.qualifiedName ?: "sign_in") {
                            popUpTo(0)
                        }
                    }
                )
            },
            bottomBar = {
                com.appvendor.main.components.CardNavigationBar(
                    currentRoute = currentRoute,
                    userPermissions = state.userPermissions,
                    userRoles = state.userRoles,
                    onNavigate = { route ->
                        nestedNavController.navigate(route) {
                            popUpTo(nestedNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = nestedNavController,
                startDestination = Routes.Dashboard::class.qualifiedName ?: "dashboard",
                modifier = Modifier.padding(paddingValues)
            ) {
                // TODO: Replace these stubs with actual screens when implementing them
                composable(Routes.Dashboard::class.qualifiedName ?: "dashboard") {
                    val dashboardViewModel: com.appvendor.feature_dashboard.presentation.DashboardViewModel = hiltViewModel()
                    com.appvendor.feature_dashboard.presentation.DashboardScreen(
                        viewModel = dashboardViewModel,
                        userName = state.userName,
                        onViewAllOrdersClick = {
                            nestedNavController.navigate(Routes.Orders::class.qualifiedName ?: "orders") {
                                popUpTo(nestedNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Routes.Orders::class.qualifiedName ?: "orders") {
                    val vm: com.appvendor.feature_orders.presentation.active.ActiveOrdersViewModel = hiltViewModel()
                    com.appvendor.feature_orders.presentation.active.ActiveOrdersScreen(
                        viewModel = vm,
                        onOrderClick = { orderId ->
                            nestedNavController.navigate("order_detail/$orderId")
                        }
                    )
                }
                composable("order_detail/{orderId}") {
                    val vm: com.appvendor.feature_orders.presentation.detail.OrderDetailViewModel = hiltViewModel()
                    com.appvendor.feature_orders.presentation.detail.OrderDetailScreen(
                        viewModel = vm,
                        onBack = { nestedNavController.popBackStack() }
                    )
                }
                composable(Routes.Categories::class.qualifiedName ?: "categories") {
                    val vm: com.appvendor.feature_categories.presentation.CategoriesViewModel = hiltViewModel()
                    com.appvendor.feature_categories.presentation.CategoriesScreen(viewModel = vm)
                }
                composable(Routes.Customizations::class.qualifiedName ?: "customizations") {
                    val vm: com.appvendor.feature_customizations.presentation.CustomizationsViewModel = hiltViewModel()
                    com.appvendor.feature_customizations.presentation.CustomizationsScreen(viewModel = vm)
                }
                composable(Routes.MenuItems::class.qualifiedName ?: "menu_items") {
                    val vm: com.appvendor.feature_menu_items.presentation.MenuItemsViewModel = hiltViewModel()
                    com.appvendor.feature_menu_items.presentation.MenuItemsScreen(
                        viewModel = vm,
                        onImportMenuClick = { nestedNavController.navigate(Routes.ImportMenu::class.qualifiedName ?: "import_menu") }
                    )
                }
                composable(Routes.Departments::class.qualifiedName ?: "departments") {
                    val vm: com.appvendor.feature_departments.presentation.DepartmentsViewModel = hiltViewModel()
                    com.appvendor.feature_departments.presentation.DepartmentsScreen(viewModel = vm)
                }
                composable(Routes.Staff::class.qualifiedName ?: "staff") {
                    val vm: com.appvendor.feature_staff.presentation.StaffViewModel = hiltViewModel()
                    com.appvendor.feature_staff.presentation.StaffScreen(viewModel = vm)
                }
                composable(Routes.Reports::class.qualifiedName ?: "reports") {
                    val vm: com.appvendor.feature_reports.presentation.ReportsViewModel = hiltViewModel()
                    com.appvendor.feature_reports.presentation.ReportsScreen(viewModel = vm)
                }
                composable(Routes.ShopProfile::class.qualifiedName ?: "shop_profile") {
                    val vm: com.appvendor.feature_shop_profile.presentation.ShopProfileViewModel = hiltViewModel()
                    com.appvendor.feature_shop_profile.presentation.ShopProfileScreen(viewModel = vm)
                }
                composable(Routes.Settings::class.qualifiedName ?: "settings") {
                    val vm: com.appvendor.feature_settings.presentation.SettingsViewModel = hiltViewModel()
                    com.appvendor.feature_settings.presentation.SettingsScreen(viewModel = vm)
                }
                composable(Routes.ListItems::class.qualifiedName ?: "list_items") {
                    // ListItemsScreen()
                }
                composable(Routes.Geofence::class.qualifiedName ?: "geofence") {
                    val vm: com.appvendor.feature_geofence.presentation.GeofenceViewModel = hiltViewModel()
                    com.appvendor.feature_geofence.presentation.GeofenceScreen(viewModel = vm)
                }
                composable(Routes.QrCode::class.qualifiedName ?: "qr_code") {
                    if (state.userPermissions.contains("qr.view")) {
                        com.appvendor.feature_qr.presentation.QrScreen()
                    }
                }
                composable(Routes.ImportMenu::class.qualifiedName ?: "import_menu") {
                    com.appvendor.feature_menu_items.presentation.import_menu.MenuImportFlow(
                        onNavigateBack = { nestedNavController.popBackStack() }
                    )
                }
                composable(Routes.PrinterSettings::class.qualifiedName ?: "printer_settings") {
                    val vm: com.appvendor.feature_printer.presentation.PrinterSettingsViewModel = hiltViewModel()
                    com.appvendor.feature_printer.presentation.PrinterSettingsScreen(viewModel = vm)
                }
            }
        }
    }
}
