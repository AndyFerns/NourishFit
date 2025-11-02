package com.example.nourishfit.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nourishfit.data.db.FoodEntity
import com.example.nourishfit.ui.viewmodel.FoodViewModel
import com.example.nourishfit.ui.viewmodel.FoodViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.NavController

// --- CHANGE: Removed onLoginClick and onLogout. AppScreen now handles this. ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DietTrackerScreenContent(
    onNavigateToCamera: () -> Unit,
    viewModel: FoodViewModel,
    navController: NavController
) {
    val foodItems by viewModel.foods.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(key1 = Unit) {
        if (currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously()
        }
    }

    // --- ALL MACROS ARE NOW CALCULATED ---
    val totalCalories = foodItems.sumOf { it.calories }
    val totalProtein = foodItems.sumOf { it.protein }
    val totalCarbs = foodItems.sumOf { it.carbs }
    val totalFat = foodItems.sumOf { it.fat }

    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    // --- State to hold the scanned food name ---
    var scannedFoodName by remember { mutableStateOf<String?>(null) }

    // Action to wait for result from camera
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(navController, lifecycleOwner.lifecycle) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        // Check if a result is available
        savedStateHandle?.getStateFlow<String>("scannedFoodName", "")
            ?.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            ?.collect { foodName ->
                if (foodName.isNotBlank()) {
                    // We got a result!
                    scannedFoodName = foodName
                    showAddDialog = true // Open the dialog
                    // Clear the result so it's not used again
                    savedStateHandle.set("scannedFoodName", "")
                }
            }
    }

    // --- CHANGE: Removed Scaffold. The layout is now a Box ---
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Apply padding for the content AND the floating action buttons
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
        ) {
            item {
                DaySwitcher(currentDate) { newDate -> viewModel.changeDate(newDate) }
                Spacer(Modifier.height(8.dp))
                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                Spacer(Modifier.height(16.dp))

                // --- THE FIX: Pass all macros to the summary card ---
                DailySummaryCard(
                    calories = totalCalories,
                    protein = totalProtein,
                    carbs = totalCarbs,
                    fat = totalFat,
                    calorieGoal = 2000 // Placeholder goal
                )
                Spacer(Modifier.height(24.dp))
            }

            val filteredItems = foodItems.filter { it.name.contains(searchQuery, ignoreCase = true) }

            if (filteredItems.isEmpty()) {
                item { EmptyState() }
            } else {
                val groupedMeals = filteredItems.groupBy { it.name.categorizeFood() }
                groupedMeals.forEach { (mealType, items) ->
                    item { MealHeader(mealType = mealType) }
                    items(items, key = { it.id }) { foodItem ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it / 2 }),
                            exit = fadeOut(animationSpec = tween(300))
                        ) {
                            // --- THE FIX: FoodListItem now shows macros ---
                            FoodListItem(
                                foodItem = foodItem,
                                onDelete = { viewModel.deleteFood(it) },
                                onEdit = { /* TODO: implement edit */ }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = onNavigateToCamera,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = "Scan Food (ML)")
            }
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Food")
            }
        }

        if (showAddDialog) {
            AddFoodDialog(
                prefilledFoodName = scannedFoodName,
                onDismiss = { showAddDialog = false },
                onAddFood = { name, calories, protein, carbs, fat ->
                    viewModel.addFood(name, calories, protein, carbs, fat)
                    scannedFoodName = null // Clear the name when food is added
                }
            )
        }
    }
}


@Composable
fun UserMenu(userName: String, onLogout: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier.padding(end = 8.dp).clickable { menuExpanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hi, $userName!", style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.Default.MoreVert, contentDescription = "User Menu")
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text("Logout") },
                onClick = {
                    menuExpanded = false
                    onLogout()
                },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, "Logout") }
            )
        }
    }
}

// --- This Composable is now updated to show macros ---
@Composable
fun DailySummaryCard(
    calories: Int,
    protein: Int,
    carbs: Int,
    fat: Int,
    calorieGoal: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Daily Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            CalorieGoalRing(caloriesConsumed = calories, calorieGoal = calorieGoal)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MacroInfo(label = "Protein", value = "${protein}g", color = Color(0xFF00C853)) // Green
                MacroInfo(label = "Carbs", value = "${carbs}g", color = Color(0xFF2979FF)) // Blue
                MacroInfo(label = "Fat", value = "${fat}g", color = Color(0xFFFFD600)) // Yellow
            }
        }
    }
}

// --- This Composable is now updated to show macros ---
@Composable
fun FoodListItem(
    foodItem: FoodEntity,
    onDelete: (FoodEntity) -> Unit,
    onEdit: (FoodEntity) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                foodItem.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            // --- NEW: Show all macros in the list item ---
            Text(
                "P: ${foodItem.protein}g, C: ${foodItem.carbs}g, F: ${foodItem.fat}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "${foodItem.calories} kcal",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = { onEdit(foodItem) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onDelete(foodItem)
        }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun AddFoodDialog(
    prefilledFoodName: String?,
    onDismiss: () -> Unit,
    onAddFood: (name: String, calories: Int, protein: Int, carbs: Int, fat: Int) -> Unit
) {
    var name by remember(prefilledFoodName) { mutableStateOf(prefilledFoodName ?: "") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Food") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name") },
                    leadingIcon = { Icon(Icons.Outlined.Fastfood, null) }
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories (kcal)") },
                    leadingIcon = { Icon(Icons.Outlined.LocalFireDepartment, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // --- NEW: Macro input fields ---
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat (g)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val cal = calories.toIntOrNull() ?: 0
                val prot = protein.toIntOrNull() ?: 0
                val carb = carbs.toIntOrNull() ?: 0
                val f = fat.toIntOrNull() ?: 0

                if (name.isNotBlank()) {
                    onAddFood(name, cal, prot, carb, f)
                }
                onDismiss()
            }) {
                Text("Add Food")
            }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CalorieGoalRing(caloriesConsumed: Int, calorieGoal: Int, strokeWidth: Dp = 12.dp, ringColor: Color = MaterialTheme.colorScheme.primary, backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer) {
    val progress = (caloriesConsumed.toFloat() / calorieGoal.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000), label = "")
    Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                drawArc(backgroundColor, -90f, 360f, false, style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round))
                drawArc(ringColor, -90f, animatedProgress * 360f, false, style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$caloriesConsumed", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = ringColor)
                Text("/ $calorieGoal kcal", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
@Composable
fun MealHeader(mealType: MealType) {
    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(mealType.icon, mealType.title, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(mealType.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
fun EmptyState() {
    Column(Modifier.fillMaxWidth().padding(vertical = 64.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.RestaurantMenu, "No food", Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Text("Nothing logged yet", style = MaterialTheme.typography.titleMedium)
        Text("Tap the '+' button to add your first meal.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(query, onQueryChange, label = { Text("Search foods...") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), leadingIcon = { Icon(Icons.Outlined.Search, "Search") }, shape = RoundedCornerShape(50))
}
@Composable
fun DaySwitcher(currentDate: LocalDate, onDateChange: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val day = currentDate.dayOfMonth
    val suffix = when { day in 11..13 -> "th"; day % 10 == 1 -> "st"; day % 10 == 2 -> "nd"; day % 10 == 3 -> "rd"; else -> "th" }
    val month = currentDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val dayOfWeek = currentDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val formattedDate = "$dayOfWeek, $month $day$suffix"
    val isToday = currentDate == LocalDate.now()
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        IconButton({ onDateChange(currentDate.minusDays(1)) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous") }
        TextButton({ DatePickerDialog(context, { _, y, m, d -> onDateChange(LocalDate.of(y, m + 1, d)) }, currentDate.year, currentDate.monthValue - 1, currentDate.dayOfMonth).show() }) {
            Text(if (isToday) "Today" else formattedDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        IconButton({ onDateChange(currentDate.plusDays(1)) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next") }
    }
}
enum class MealType(val title: String, val icon: ImageVector) { BREAKFAST("Breakfast", Icons.Outlined.BreakfastDining), LUNCH("Lunch", Icons.Outlined.LunchDining), DINNER("Dinner", Icons.Outlined.DinnerDining), SNACK("Snacks", Icons.Outlined.Fastfood), UNKNOWN("Other", Icons.Outlined.Restaurant) }
fun String.categorizeFood(): MealType {
    val lower = lowercase(Locale.getDefault())
    return when {
        listOf("egg", "toast", "cereal", "oats").any { lower.contains(it) } -> MealType.BREAKFAST
        listOf("salad", "sandwich", "soup").any { lower.contains(it) } -> MealType.LUNCH
        listOf("chicken", "salmon", "steak", "pasta").any { lower.contains(it) } -> MealType.DINNER
        listOf("apple", "banana", "yogurt", "nuts").any { lower.contains(it) } -> MealType.SNACK
        else -> MealType.UNKNOWN
    }
}
@Composable
fun MacroInfo(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}