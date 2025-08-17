package com.example.nourishfit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nourishfit.ui.screens.DietTrackerScreen
import com.example.nourishfit.ui.screens.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object DietTracker : Screen("diet_tracker")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onStartTrackingClick = {
                navController.navigate(Screen.DietTracker.route)
            })
        }
        composable(Screen.DietTracker.route) {
            DietTrackerScreen()
        }
    }
}