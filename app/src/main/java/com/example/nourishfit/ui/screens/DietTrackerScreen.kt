package com.example.nourishfit.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateUp: () -> Unit = {},
    onLoginClick: () -> Unit = {},
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
                title = { Text("Today's Diet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    if (isAnonymous) {
                        TextButton(onClick = onLoginClick) {
                            Text("Login to Sync")
                        }
                    } else {
                        val userName = currentUser?.email?.substringBefore('@') ?: "User"
                        Text(
                            text = "Hi, $userName!",
                            modifier = Modifier.padding(end = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Food")
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
                CalorieGoalRing(caloriesConsumed = totalCalories, calorieGoal = 2000) // Using a placeholder goal
                Spacer(Modifier.height(24.dp))
            }

            val filteredItems = foodItems.filter { it.name.contains(searchQuery, ignoreCase = true) }

            if (filteredItems.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                val groupedMeals = filteredItems.groupBy { it.name.categorizeFood() } // A little UI magic
                groupedMeals.forEach { (mealType, items) ->
                    item {
                        MealHeader(mealType = mealType)
                    }
                    items(items, key = { it.id }) { foodItem ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it / 2 }),
                            exit = fadeOut(animationSpec = tween(300))
                        ) {
                            FoodListItem(
                                foodItem = foodItem,
                                onDelete = { viewModel.deleteFood(it) },
                                onEdit = { /* TODO: implement edit */ }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
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

// --- NEW & PRETTIFIED COMPOSABLES ---

@Composable
fun CalorieGoalRing(
    caloriesConsumed: Int,
    calorieGoal: Int,
    strokeWidth: Dp = 12.dp,
    ringColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer
) {
    val progress = (caloriesConsumed.toFloat() / calorieGoal.toFloat()).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "CalorieProgressAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val startAngle = -90f
                val sweepAngle = animatedProgress * 360f

                // Background ring
                drawArc(
                    color = backgroundColor,
                    startAngle = startAngle,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )

                // Progress ring
                drawArc(
                    color = ringColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = caloriesConsumed.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = ringColor
                )
                Text(
                    text = "/ $calorieGoal kcal",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MealHeader(mealType: MealType) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = mealType.icon,
                contentDescription = mealType.title,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = mealType.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

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
        }
        Text(
            "${foodItem.calories} kcal",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = { onEdit(foodItem) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onDelete(foodItem)
        }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.RestaurantMenu,
            contentDescription = "No food logged",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nothing logged yet",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Tap the '+' button to add your first meal.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AddFoodDialog(
    onDismiss: () -> Unit,
    onAddFood: (name: String, calories: Int) -> Unit
) {
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
                    leadingIcon = { Icon(Icons.Outlined.Fastfood, contentDescription = null) }
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories (kcal)") },
                    leadingIcon = { Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val cal = calories.toIntOrNull()
                if (name.isNotBlank() && cal != null) {
                    onAddFood(name, cal)
                }
                onDismiss()
            }) {
                Text("Add Food")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// --- EXISTING COMPOSABLES (Minor Style Tweaks) ---

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search foods...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search Icon") },
        shape = RoundedCornerShape(50) // Pill shape
    )
}

@Composable
fun DaySwitcher(
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    val context = LocalContext.current

    val day = currentDate.dayOfMonth
    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
    val month = currentDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val dayOfWeek = currentDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val formattedDate = "$dayOfWeek, $month $day$suffix"
    val isToday = currentDate == LocalDate.now()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onDateChange(currentDate.minusDays(1)) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
        }

        TextButton(onClick = {
            val listener = DatePickerDialog.OnDateSetListener { _, year, monthIndex, dayOfMonth ->
                onDateChange(LocalDate.of(year, monthIndex + 1, dayOfMonth))
            }
            DatePickerDialog(
                context, listener, currentDate.year,
                currentDate.monthValue - 1, currentDate.dayOfMonth
            ).show()
        }) {
            Text(
                text = if (isToday) "Today" else formattedDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        IconButton(onClick = { onDateChange(currentDate.plusDays(1)) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
        }
    }
}

// --- HELPER ENUM & FUNCTION FOR UI ---
enum class MealType(val title: String, val icon: ImageVector) {
    BREAKFAST("Breakfast", Icons.Outlined.BreakfastDining),
    LUNCH("Lunch", Icons.Outlined.LunchDining),
    DINNER("Dinner", Icons.Outlined.DinnerDining),
    SNACK("Snacks", Icons.Outlined.Fastfood),
    UNKNOWN("Other", Icons.Outlined.Restaurant)
}

// A simple (and not very smart) way to guess the meal type from the name for the UI.
fun String.categorizeFood(): MealType {
    val lowerCaseName = this.lowercase(Locale.getDefault())
    return when {
        listOf("egg", "toast", "cereal", "oats", "pancake").any { lowerCaseName.contains(it) } -> MealType.BREAKFAST
        listOf("salad", "sandwich", "soup", "wrap").any { lowerCaseName.contains(it) } -> MealType.LUNCH
        listOf("chicken", "salmon", "steak", "pasta", "rice").any { lowerCaseName.contains(it) } -> MealType.DINNER
        listOf("apple", "banana", "yogurt", "nuts", "bar").any { lowerCaseName.contains(it) } -> MealType.SNACK
        else -> MealType.UNKNOWN
    }
}

