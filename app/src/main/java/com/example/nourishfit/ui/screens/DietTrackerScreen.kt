package com.example.nourishfit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nourishfit.ui.theme.NourishFitTheme

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
    onLoginClick: () -> Unit = {} // New parameter to handle login navigation
) {
    // State to hold the list of food items for the day.
    // In a real app, you would get this from a ViewModel or a database.
    val foodItems = remember {
        mutableStateListOf(
            FoodItem(1, "Scrambled Eggs", "Breakfast", 150, 13, 1, 11),
            FoodItem(2, "Whole Wheat Toast", "Breakfast", 80, 4, 14, 1),
            FoodItem(3, "Grilled Chicken Salad", "Lunch", 350, 40, 10, 18),
            FoodItem(4, "Apple", "Snack", 95, 0, 25, 0),
            FoodItem(5, "Salmon with Quinoa", "Dinner", 500, 45, 30, 20)
        )
    }

    // Calculate total macros for the summary
    val totalCalories = foodItems.sumOf { it.calories }
    val totalProtein = foodItems.sumOf { it.protein }
    val totalCarbs = foodItems.sumOf { it.carbs }
    val totalFat = foodItems.sumOf { it.fat }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Today's Diet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                // Action button added to the top bar
                actions = {
                    TextButton(onClick = onLoginClick) {
                        Text("Login")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // TODO: Implement logic to add a new food item.
                }
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
            contentPadding = PaddingValues(bottom = 80.dp) // Padding for the FAB
        ) {
            // --- Daily Summary Section ---
            item {
                DailySummaryCard(totalCalories, totalProtein, totalCarbs, totalFat)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- Meals Section ---
            val groupedMeals = foodItems.groupBy { it.mealType }

            groupedMeals.forEach { (mealType, items) ->
                item {
                    Text(
                        text = mealType,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(items, key = { it.id }) { foodItem ->
                    FoodListItem(foodItem = foodItem)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
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
fun FoodListItem(foodItem: FoodItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = foodItem.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(
                text = "P: ${foodItem.protein}g, C: ${foodItem.carbs}g, F: ${foodItem.fat}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${foodItem.calories} kcal",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )
    }
}


// --- PREVIEW FUNCTION ---
@Preview(showBackground = true)
@Composable
fun DietTrackerScreenPreview() {
    NourishFitTheme {
        DietTrackerScreen()
    }
}
