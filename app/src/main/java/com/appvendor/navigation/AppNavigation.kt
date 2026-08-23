package com.appvendor.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.appvendor.core.navigation.Routes
import com.appvendor.feature_auth.presentation.signin.SignInScreen
import com.appvendor.feature_auth.presentation.signin.SignInViewModel
import com.appvendor.feature_auth.presentation.signup.SignUpScreen
import com.appvendor.feature_auth.presentation.signup.SignUpViewModel
import com.appvendor.main.MainScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SignIn::class.qualifiedName ?: "sign_in"
    ) {

        // ── Sign In ───────────────────────────────────────────────────────────
        composable(route = Routes.SignIn::class.qualifiedName ?: "sign_in") {
            val viewModel: SignInViewModel = hiltViewModel()
            SignInScreen(
                viewModel = viewModel,
                onNavigateToSignUp = {
                    navController.navigate(Routes.SignUp::class.qualifiedName ?: "sign_up")
                },
                onNavigateToMain = {
                    navController.navigate(Routes.Dashboard::class.qualifiedName ?: "dashboard") {
                        popUpTo(Routes.SignIn::class.qualifiedName ?: "sign_in") { inclusive = true }
                    }
                }
            )
        }

        // ── Sign Up ───────────────────────────────────────────────────────────
        composable(route = Routes.SignUp::class.qualifiedName ?: "sign_up") {
            val viewModel: SignUpViewModel = hiltViewModel()
            SignUpScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Main (Dashboard + Drawer) ─────────────────────────────────────────
        composable(route = Routes.Dashboard::class.qualifiedName ?: "dashboard") {
            MainScreen(mainNavController = navController)
        }
    }
}
