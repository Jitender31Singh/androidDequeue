package com.appvendor.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the app.
 *
 * Each route is a @Serializable object/class so it works with
 * the Navigation Compose type-safe API (Navigation 2.8+).
 *
 * Usage:
 *   navController.navigate(Routes.Dashboard)
 *   composable<Routes.Dashboard> { ... }
 */
sealed class Routes {

    // ── Auth graph ───────────────────────────────────────────────
    @Serializable
    data object SignIn : Routes()

    @Serializable
    data object SignUp : Routes()

    @Serializable
    data class Otp(val email: String) : Routes()

    // ── Main graph ───────────────────────────────────────────────
    @Serializable
    data object Dashboard : Routes()

    @Serializable
    data object ListItems : Routes()

    @Serializable
    data object Geofence : Routes()

    @Serializable
    data object Orders : Routes()

    @Serializable
    data class OrderDetail(val orderId: String) : Routes()

    @Serializable
    data object Categories : Routes()

    @Serializable
    data object Customizations : Routes()

    @Serializable
    data object MenuItems : Routes()

    @Serializable
    data object Departments : Routes()

    @Serializable
    data object Staff : Routes()

    @Serializable
    data object Reports : Routes()

    @Serializable
    data object ShopProfile : Routes()

    @Serializable
    data object Settings : Routes()

    @Serializable
    data object QrCode : Routes()

    @Serializable
    data object ImportMenu : Routes()

    @Serializable
    data object PrinterSettings : Routes()
}
