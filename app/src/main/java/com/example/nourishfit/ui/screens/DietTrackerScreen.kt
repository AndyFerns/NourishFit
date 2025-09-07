package com.example.nourishfit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nourishfit.ui.theme.NourishFitTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext

// A simple data class to represent a food item.
// In a real app, this would likely be more complex.
data class FoodItem(
    val id: Int,
    val name: String,
    val mealType: String, // e.g., "Breakfast", "Lunch"
    val calories: Int,
    val protein: Int, // in grams
    val carbs: Int,   // in grams
    val fat: Int      // in grams
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietTrackerScreen(
    onNavigateUp: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val foodItems = remember {
        mutableStateListOf(
            FoodItem(1, "Scrambled Eggs", "Breakfast", 150, 13, 1, 11),
            FoodItem(2, "Whole Wheat Toast", "Breakfast", 80, 4, 14, 1),
            FoodItem(3, "Grilled Chicken Salad", "Lunch", 350, 40, 10, 18),
            FoodItem(4, "Apple", "Snack", 95, 0, 25, 0),
            FoodItem(5, "Salmon with Quinoa", "Dinner", 500, 45, 30, 20)
        )
    }

    val totalCalories = foodItems.sumOf { it.calories }
    val totalProtein = foodItems.sumOf { it.protein }
    val totalCarbs = foodItems.sumOf { it.carbs }
    val totalFat = foodItems.sumOf { it.fat }

    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Today's Diet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    TextButton(onClick = onLoginClick) {
                        Text("Login")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
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
                DaySwitcher(currentDate) { currentDate = it }
                Spacer(Modifier.height(8.dp))
                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                Spacer(Modifier.height(16.dp))
                DailySummaryCard(totalCalories, totalProtein, totalCarbs, totalFat)
                Spacer(Modifier.height(24.dp))
            }

            val filteredItems =
                foodItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
            val groupedMeals = filteredItems.groupBy { it.mealType }

            groupedMeals.forEach { (mealType, items) ->
                item {
                    Text(
                        mealType,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(items, key = { it.id }) { foodItem ->
                    FoodListItem(
                        foodItem = foodItem,
                        onDelete = { foodItems.remove(it) },
                        onEdit = { /* TODO: implement edit */ }
                    )
                    Divider(Modifier.padding(vertical = 8.dp))
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }

        // Place dialog OUTSIDE LazyColumn
        if (showAddDialog) {
            AddFoodDialog(
                onDismiss = { showAddDialog = false },
                onAddFood = { newFood -> foodItems.add(newFood) }
            )
        }
    }
}

@Composable
fun DailySummaryCard(calories: Int, protein: Int, carbs: Int, fat: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Daily Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MacroInfo(label = "Calories", value = calories.toString())
                MacroInfo(label = "Protein", value = "${protein}g")
                MacroInfo(label = "Carbs", value = "${carbs}g")
                MacroInfo(label = "Fat", value = "${fat}g")
            }
        }
    }
}

@Composable
fun MacroInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FoodListItem(
    foodItem: FoodItem,
    onDelete: (FoodItem) -> Unit,
    onEdit: (FoodItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                foodItem.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
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
            fontSize = 18.sp
        )

        IconButton(onClick = { onEdit(foodItem) }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
        IconButton(onClick = { onDelete(foodItem) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
fun AddFoodDialog(
    onDismiss: () -> Unit,
    onAddFood: (FoodItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Breakfast") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Food") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Food Name") })
                OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Calories") })
                OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("Protein (g)") })
                OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Carbs (g)") })
                OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("Fat (g)") })

                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(mealType)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    mealType = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onAddFood(
                        FoodItem(
                            id = (0..100000).random(),
                            name = name,
                            mealType = mealType,
                            calories = calories.toIntOrNull() ?: 0,
                            protein = protein.toIntOrNull() ?: 0,
                            carbs = carbs.toIntOrNull() ?: 0,
                            fat = fat.toIntOrNull() ?: 0
                        )
                    )
                }
                onDismiss()
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search foods") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun DaySwitcher(
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    val context = LocalContext.current

    // Format date as "8th September, Monday"
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
    val formattedDate = "$day$suffix $month, $dayOfWeek"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onDateChange(currentDate.minusDays(1)) }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Previous Day")
        }

        // Center date with clickable calendar popup
        TextButton(onClick = {
            val listener = DatePickerDialog.OnDateSetListener { _, year, monthIndex, dayOfMonth ->
                val newDate = LocalDate.of(year, monthIndex + 1, dayOfMonth)
                onDateChange(newDate)
            }

            DatePickerDialog(
                context,
                listener,
                currentDate.year,
                currentDate.monthValue - 1,
                currentDate.dayOfMonth
            ).show()
        }) {
            Text(formattedDate, style = MaterialTheme.typography.titleMedium)
        }

        IconButton(onClick = { onDateChange(currentDate.plusDays(1)) }) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Next Day")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DietTrackerScreenPreview() {
    NourishFitTheme {
        DietTrackerScreen()
    }
}
