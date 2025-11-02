package com.example.nourishfit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.nourishfit.ui.screens.AppScreen
import com.example.nourishfit.ui.screens.HomeScreen
import com.example.nourishfit.ui.screens.CameraScreen
import com.example.nourishfit.ui.screens.ChatScreen
import com.example.nourishfit.ui.screens.LoginScreen

import com.example.nourishfit.ui.viewmodel.FoodViewModelFactory
import com.example.nourishfit.ui.viewmodel.ProgressViewModelFactory
import com.example.nourishfit.ui.viewmodel.StepTrackerViewModelFactory

// --- THE CHANGE: This is now a simpler, high-level navigation graph ---
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object App : Screen("app") // Represents the main app container with the bottom bar
    object Chat : Screen("chat")
    object Camera : Screen("camera")
}

@Composable
fun AppNavigation(
    foodViewModelFactory: FoodViewModelFactory,
    stepTrackerViewModelFactory: StepTrackerViewModelFactory,
    progressViewModelFactory: ProgressViewModelFactory
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onStartTrackingClick = {
                // From Home, we go to the main App container
                navController.navigate(Screen.App.route) {
                    // Prevent going back to the Home screen
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // After login, go to the main App screen and clear the entire history
                    navController.navigate(Screen.App.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }
        // This is the new main destination for your app's core features.
        composable(Screen.App.route) {
            AppScreen(
                foodViewModelFactory = foodViewModelFactory,
                stepTrackerViewModelFactory = stepTrackerViewModelFactory,
                progressViewModelFactory = progressViewModelFactory,

                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onLogout = {
                    // On logout, go back to the Home screen and clear the history
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route)
                },
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateUp = {
                    navController.navigateUp()
                }
                // We use the default viewModel() here since ChatViewModel has no factory
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onFoodScanned = { foodName ->
                    // Send the result back to the DietTrackerScreen
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scannedFoodName", foodName)
                    navController.popBackStack()
                }
            )
        }
    }
}

