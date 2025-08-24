package com.example.nourishfit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nourishfit.ui.screens.DietTrackerScreen
import com.example.nourishfit.ui.screens.HomeScreen
import com.example.nourishfit.ui.screens.LoginScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object DietTracker : Screen("diet_tracker")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route // Start at the login screen
    ) {
        composable(Screen.Login.route) {
            LoginScreen(onLoginClick = {
                // Navigate to home and clear the back stack so the user can't go back to login
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(onStartTrackingClick = {
                navController.navigate(Screen.DietTracker.route)
            })
        }
        composable(Screen.DietTracker.route) {
            // Here's the key change:
            // We pass the navController's navigateUp function to the screen.
            DietTrackerScreen(onNavigateUp = {
                navController.navigateUp()
            })
        }
    }
}
