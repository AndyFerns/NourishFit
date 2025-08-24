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
        startDestination = Screen.Home.route // App now starts at the Home screen
    ) {
        composable(Screen.Login.route) {
            LoginScreen(onLoginClick = {
                // After login, go to home and clear the login screen from the back stack
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
            DietTrackerScreen(
                onNavigateUp = {
                    navController.navigateUp()
                },
                // Connect the new button to the navigation controller
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }
    }
}
