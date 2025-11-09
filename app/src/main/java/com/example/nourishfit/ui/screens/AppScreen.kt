package com.example.nourishfit.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.compose.*
import com.example.nourishfit.navigation.BottomNavItem
import com.example.nourishfit.ui.viewmodel.FoodViewModelFactory
import com.example.nourishfit.ui.viewmodel.ProgressViewModelFactory
import com.example.nourishfit.ui.viewmodel.StepTrackerViewModelFactory
import com.example.nourishfit.ui.viewmodel.ProfileViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    foodViewModelFactory: FoodViewModelFactory,
    stepTrackerViewModelFactory: StepTrackerViewModelFactory,
    progressViewModelFactory: ProgressViewModelFactory,
    profileViewModelFactory: ProfileViewModelFactory,

    // For the Login Screen
    onNavigateToLogin: () -> Unit,
    onLogout: () -> Unit,

    // For the Chatbot
    onNavigateToChat: () -> Unit,
    onNavigateToCamera: () -> Unit,

    // For profile screen
    onNavigateToProfile: () -> Unit,

    // This is the non-nullable NavBackStackEntry from the *outer* navigator
    navBackStackEntry: NavBackStackEntry
) {
    // This is the NavController for the *inner* bottom bar navigation
    val navController = rememberNavController()

    // for haptic feedback
    val haptics = LocalHapticFeedback.current

    // --- THE FIX: Rename this variable to avoid the name collision ---
    val innerNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = innerNavBackStackEntry?.destination
    // --- END OF FIX ---

    var scannedFoodName by remember { mutableStateOf<String?>(null) }

    // --- It correctly references the non-nullable 'navBackStackEntry' parameter ---
    LaunchedEffect(navBackStackEntry, navBackStackEntry.lifecycle) {
        val savedStateHandle = navBackStackEntry.savedStateHandle
        savedStateHandle.getStateFlow<String>("scannedFoodName", "")
            .flowWithLifecycle(navBackStackEntry.lifecycle, Lifecycle.State.RESUMED)
            .collect { foodName ->
                if (foodName.isNotBlank()) {
                    scannedFoodName = foodName
                    savedStateHandle.set("scannedFoodName", "")
                }
            }
    }


    val items = listOf(
        BottomNavItem.Diet,
        BottomNavItem.Steps,
        BottomNavItem.Workouts,
        BottomNavItem.Progress,
        BottomNavItem.Settings,
    )

    // --- This is now the ONLY Scaffold in the main app ---
    Scaffold(
        topBar = {
            // --- THE FIX: Use the renamed variable here ---
            val title = items.find { it.route == currentDestination?.route }?.title ?: "NourishFit"
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
                // --- THE FIX: Use the renamed variable here ---
                val currentInnerDestination = innerNavBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        // --- THE FIX: Use the renamed variable here ---
                        selected = currentInnerDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            // Haptic feedback implementation
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)

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
                    viewModel = viewModel(factory = foodViewModelFactory),
                    onNavigateToCamera = onNavigateToCamera,
                    prefilledFoodName = scannedFoodName,
                    onDialogDismissed = {
                        scannedFoodName = null
                    }
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
                    viewModel = viewModel(factory = progressViewModelFactory),
                    onNavigateToChat = onNavigateToChat
                )
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreenContent(
                    onNavigateToProfile = onNavigateToProfile
                )
            }
        }
    }
}

