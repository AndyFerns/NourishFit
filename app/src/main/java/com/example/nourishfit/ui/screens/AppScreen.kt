package com.example.nourishfit.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.nourishfit.navigation.BottomNavItem
import com.example.nourishfit.ui.viewmodel.FoodViewModelFactory
import com.example.nourishfit.ui.viewmodel.ProgressViewModelFactory
import com.example.nourishfit.ui.viewmodel.StepTrackerViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    // --- THE FIX: Add the missing factories as parameters ---
    foodViewModelFactory: FoodViewModelFactory,
    stepTrackerViewModelFactory: StepTrackerViewModelFactory,
    progressViewModelFactory: ProgressViewModelFactory,
    onNavigateToLogin: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavItem.Diet,
        BottomNavItem.Steps,
        BottomNavItem.Workouts,
        BottomNavItem.Progress,
        BottomNavItem.Settings,
    )

    Scaffold(
        topBar = {
            // This TopAppBar is now dynamic and will show the correct title
            val title = items.find { it.route == currentDestination?.route }?.title ?: "NourishFit"
            TopAppBar(title = { Text(title) })
        },
        bottomBar = {
            NavigationBar {
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
                // We now call the '...Content' version of the screen
                DietTrackerScreenContent(
                    viewModel = viewModel(factory = foodViewModelFactory),
                    onLoginClick = onNavigateToLogin,
                    onLogout = onLogout
                )
            }
            composable(BottomNavItem.Steps.route) {
                // --- THE FIX: We explicitly create the ViewModel using its factory. ---
                StepTrackerScreenContent(viewModel(factory = stepTrackerViewModelFactory))
            }
            composable(BottomNavItem.Workouts.route) {
                WorkoutScreenContent()
            }
            composable(BottomNavItem.Progress.route) {
                // --- THE FIX: We explicitly create the ViewModel using its factory. ---
                ProgressScreenContent(viewModel(factory = progressViewModelFactory))
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreenContent()
            }
        }
    }
}

