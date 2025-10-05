package com.example.nourishfit.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nourishfit.navigation.BottomNavItem
import com.example.nourishfit.ui.viewmodel.FoodViewModelFactory

@Composable
fun AppScreen(
    foodViewModelFactory: FoodViewModelFactory,
    onNavigateToLogin: () -> Unit,
    // --- THE CHANGE: Add the onLogout callback parameter ---
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem.Diet,
        BottomNavItem.Steps,
        BottomNavItem.Workouts,
        BottomNavItem.Progress,
        BottomNavItem.Settings,
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.Diet.route,
            Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Diet.route) {
                DietTrackerScreen(
                    viewModelFactory = foodViewModelFactory,
                    onLoginClick = onNavigateToLogin,
                    // --- THE CHANGE: Pass the onLogout lambda down to the screen ---
                    onLogout = onLogout
                )
            }
            composable(BottomNavItem.Steps.route) {
                StepTrackerScreen()
            }
            composable(BottomNavItem.Workouts.route) {
                WorkoutScreen()
            }
            composable(BottomNavItem.Progress.route) {
                ProgressScreen()
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

