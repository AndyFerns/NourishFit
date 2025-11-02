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
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    foodViewModelFactory: FoodViewModelFactory,
    stepTrackerViewModelFactory: StepTrackerViewModelFactory,
    progressViewModelFactory: ProgressViewModelFactory,
    // For the Login Scren
    onNavigateToLogin: () -> Unit,
    onLogout: () -> Unit,
    // For the Chatbot
    onNavigateToChat: () -> Unit
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

    // --- This is now the ONLY Scaffold in your main app ---
    Scaffold(
        topBar = {
            // We find the title of the current screen
            val title = items.find { it.route == currentDestination?.route }?.title ?: ""

            // We check the auth state *here* to decide what to show in the TopAppBar
            val currentUser = FirebaseAuth.getInstance().currentUser
            val isAnonymous = currentUser?.isAnonymous ?: true

            TopAppBar(
                title = { Text(title) },
                actions = {
                    // --- ALL AUTH LOGIC IS NOW IN THE MAIN APP BAR ---
                    if (currentDestination?.route == BottomNavItem.Diet.route) {
                        if (isAnonymous) {
                            Button(onClick = onNavigateToLogin) {
                                Text("Login to Sync")
                            }
                        } else {
                            UserMenu(
                                userName = currentUser?.email?.substringBefore('@') ?: "User",
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            )
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
                DietTrackerScreenContent(
                    viewModel = viewModel(factory = foodViewModelFactory)
                    // Note: onLoginClick and onLogout are no longer needed here,
                    // because the TopAppBar in AppScreen is now handling them.
                )
            }
            composable(BottomNavItem.Steps.route) {
                StepTrackerScreenContent(viewModel(factory = stepTrackerViewModelFactory))
            }
            composable(BottomNavItem.Workouts.route) {
                WorkoutScreenContent()
            }
            composable(BottomNavItem.Progress.route) {
                ProgressScreenContent(
                    viewModel(factory = progressViewModelFactory),
                    onNavigateToChat = onNavigateToChat
                )
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreenContent()
            }
        }
    }
}

