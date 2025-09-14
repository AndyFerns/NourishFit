package com.example.nourishfit.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nourishfit.data.db.FoodEntity
import com.example.nourishfit.ui.viewmodel.FoodViewModel
import com.example.nourishfit.ui.viewmodel.FoodViewModelFactory
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel

// The local FoodItem data class is no longer needed. We will use FoodEntity from the database.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietTrackerScreen(
    onNavigateUp: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModelFactory: FoodViewModelFactory
) {
    // 1. Create an instance of the ViewModel using the factory.
    val viewModel: FoodViewModel = viewModel(factory = viewModelFactory)

    // 2. Collect the data directly from the ViewModel's StateFlows.
    // The UI will now automatically update whenever this data changes in the database.
    val foodItems by viewModel.foods.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()

    // 3. Check the curr UserState
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isAnonymous = currentUser?.isAnonymous ?: true

    // 4. Sign in the user anonymously if they arent logged in at all
    LaunchedEffect(key1 = Unit) {
        if (currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously()
        }
    }

    // 5. The UI now calculates totals based on the REAL data from the ViewModel.
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
                // 4. User actions now call functions on the ViewModel.
                DaySwitcher(currentDate) { newDate -> viewModel.changeDate(newDate) }
                Spacer(Modifier.height(8.dp))
                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                Spacer(Modifier.height(16.dp))
                // The summary card is simplified for your FoodEntity.
                DailySummaryCard(totalCalories)
                Spacer(Modifier.height(24.dp))
            }

            // The list now displays the filtered items from the database.
            items(foodItems.filter { it.name.contains(searchQuery, ignoreCase = true) }, key = { it.id }) { foodItem ->
                FoodListItem(
                    foodItem = foodItem,
                    // 5. Deleting calls the ViewModel, which removes the item from the database.
                    onDelete = { viewModel.deleteFood(it) },
                    onEdit = { /* TODO: implement edit */ }
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }
        }

        if (showAddDialog) {
            AddFoodDialog(
                onDismiss = { showAddDialog = false },
                // 6. Adding a food calls the ViewModel, which inserts it into the database.
                onAddFood = { name, calories -> viewModel.addFood(name, calories) }
            )
        }
    }
}

// Simplified to match FoodEntity (which only has calories)
@Composable
fun DailySummaryCard(calories: Int) {
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
            MacroInfo(label = "Calories", value = calories.toString())
        }
    }
}

@Composable
fun MacroInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

// Updated to display a FoodEntity from database
@Composable
fun FoodListItem(
    foodItem: FoodEntity,
    onDelete: (FoodEntity) -> Unit,
    onEdit: (FoodEntity) -> Unit
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

// Updated to provide the correct data for a new FoodEntity
@Composable
fun AddFoodDialog(
    onDismiss: () -> Unit,
    onAddFood: (name: String, calories: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Food") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Food Name") })
                OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Calories") })
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
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// No changes needed for these Composables below
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
        }
        TextButton(onClick = {
            val listener = DatePickerDialog.OnDateSetListener { _, year, monthIndex, dayOfMonth ->
                onDateChange(LocalDate.of(year, monthIndex + 1, dayOfMonth))
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
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
        }
    }
}
