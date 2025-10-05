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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DietTrackerScreen(
    onNavigateUp: (() -> Unit)? = null,
    onLoginClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModelFactory: FoodViewModelFactory
) {
    val viewModel: FoodViewModel = viewModel(factory = viewModelFactory)
    val foodItems by viewModel.foods.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isAnonymous = currentUser?.isAnonymous ?: true

    LaunchedEffect(key1 = Unit) {
        if (currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously()
        }
    }

    val totalCalories = foodItems.sumOf { it.calories }
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Diet Tracker") },
                navigationIcon = {
                    if (onNavigateUp != null) {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                        }
                    }
                },
                actions = {
                    if (isAnonymous) {
                        Button(onClick = onLoginClick) {
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { /* TODO: Launch camera for ML */ },
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                DaySwitcher(currentDate) { newDate -> viewModel.changeDate(newDate) }
                Spacer(Modifier.height(8.dp))
                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                Spacer(Modifier.height(16.dp))
                CalorieGoalRing(caloriesConsumed = totalCalories, calorieGoal = 2000)
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
                            enter = fadeIn(tween(300)) + slideInVertically { it / 2 },
                            exit = fadeOut(tween(300))
                        ) {
                            FoodListItem(
                                foodItem = foodItem,
                                onDelete = { viewModel.deleteFood(it) },
                                onEdit = { /* TODO */ }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }

        if (showAddDialog) {
            AddFoodDialog(
                onDismiss = { showAddDialog = false },
                onAddFood = { name, calories -> viewModel.addFood(name, calories) }
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

@Composable
fun AddFoodDialog(onDismiss: () -> Unit, onAddFood: (name: String, calories: Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
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
                    // --- THIS IS THE FIX ---
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = { Button({ val cal = calories.toIntOrNull(); if (name.isNotBlank() && cal != null) { onAddFood(name, cal) }; onDismiss() }) { Text("Add Food") } },
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
fun FoodListItem(foodItem: FoodEntity, onDelete: (FoodEntity) -> Unit, onEdit: (FoodEntity) -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(foodItem.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        Text("${foodItem.calories} kcal", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        IconButton({ onEdit(foodItem) }, Modifier.size(24.dp)) { Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        IconButton({ haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); onDelete(foodItem) }, Modifier.size(24.dp)) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
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