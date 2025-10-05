package com.example.nourishfit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

// A sealed class to define the items in our Bottom Navigation Bar
sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Diet : BottomNavItem("diet", "Diet", Icons.Outlined.RestaurantMenu)
    object Steps : BottomNavItem("steps", "Steps", Icons.Outlined.DirectionsRun)
    object Workouts : BottomNavItem("workouts", "Workouts", Icons.Outlined.FitnessCenter) // New Screen
    object Progress : BottomNavItem("progress", "Progress", Icons.Outlined.Assessment)   // New Screen
    object Settings : BottomNavItem("settings", "Settings", Icons.Outlined.Settings) // New Screen
}