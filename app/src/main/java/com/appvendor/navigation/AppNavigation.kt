package com.appvendor.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.appvendor.core.datastore.UserPreferences
import com.appvendor.core.datastore.dataStore
import com.appvendor.core.navigation.Routes
import com.appvendor.feature_auth.presentation.signin.SignInScreen
import com.appvendor.feature_auth.presentation.signin.SignInViewModel
import com.appvendor.feature_auth.presentation.signup.SignUpScreen
import com.appvendor.feature_auth.presentation.signup.SignUpViewModel
import com.appvendor.main.MainScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferences = remember { UserPreferences(context.dataStore) }
    val token by userPreferences.userToken.collectAsState(initial = null)
    
    var isReady by remember { mutableStateOf(false) }
    
    LaunchedEffect(token) {
        // Simple delay to ensure we've read from datastore
        isReady = true
    }
    
    if (!isReady) return

    NavHost(
        navController = navController,
        startDestination = if (token.isNullOrEmpty()) (Routes.SignIn::class.qualifiedName ?: "sign_in") else (Routes.Dashboard::class.qualifiedName ?: "dashboard")
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
