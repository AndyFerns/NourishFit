package com.example.nourishfit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

// --- Added more glanceable info to the data class ---
data class Workout(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val durationInMinutes: Int,
    val difficulty: String,
    val category: String, // For filtering
    val exercises: List<Pair<String, String>> // eg. Pair("Pushups", "3 Sets of 10")
)

val workoutPlans = listOf(
    Workout(
        id = 1,
        title = "Full Body Strength",
        description = "Comprehensive workout for all muscle groups.",
        imageUrl = "https://images.pexels.com/photos/1552242/pexels-photo-1552242.jpeg?auto=compress&cs=tinysrgb&w=600",
        durationInMinutes = 45,
        difficulty = "Intermediate",
        category = "Strength",
        exercises = listOf(
            Pair("Squats", "3 sets of 10-12 reps"),
            Pair("Bench Press", "3 sets of 8-10 reps"),
            Pair("Pull-Ups / Lat Pulldowns", "3 sets to failure"),
            Pair("Overhead Press", "3 sets of 10 reps"),
            Pair("Plank", "3 sets of 60 seconds")
        )
    ),
    Workout(
        id = 2,
        title = "Cardio Blast",
        description = "High-intensity interval training to boost your heart rate.",
        imageUrl = "https://images.pexels.com/photos/6455847/pexels-photo-6455847.jpeg?auto=compress&cs=tinysrgb&w=600",
        durationInMinutes = 30,
        difficulty = "Hard",
        category = "Cardio",
        exercises = listOf(
            Pair("Warm-up", "5 minutes jogging"),
            Pair("Jumping Jacks", "4 sets of 45 seconds"),
            Pair("Burpees", "4 sets of 15 reps"),
            Pair("High Knees", "4 sets of 45 seconds"),
            Pair("Cool-down", "5 minutes stretching")
        )
    ),
    Workout(
        id = 3,
        title = "Upper Body Focus",
        description = "Target your chest, back, and arms.",
        imageUrl = "https://images.pexels.com/photos/29205095/pexels-photo-29205095.jpeg?auto=compress&cs=tinysrgb&w=600",
        durationInMinutes = 40,
        difficulty = "Intermediate",
        category = "Strength",
        exercises = listOf(
            Pair("Push-ups", "4 sets to failure"),
            Pair("Dumbbell Rows", "3 sets of 12 reps per arm"),
            Pair("Bicep Curls", "3 sets of 15 reps"),
            Pair("Tricep Dips", "3 sets of 15 reps")
        )
    ),
    Workout(
        id = 4,
        title = "Core & Flexibility",
        description = "Improve your core strength and flexibility.",
        imageUrl = "https://images.pexels.com/photos/1756959/pexels-photo-1756959.jpeg?auto=compress&cs=tinysrgb&w=600",
        durationInMinutes = 25,
        difficulty = "Beginner",
        category = "Flexibility",
        exercises = listOf(
            Pair("Cobra Stretch", "3 sets of 30 seconds"),
            Pair("Plank", "3 sets of 60 seconds"),
            Pair("Bird-Dog", "3 sets of 12 reps per side"),
            Pair("Child's Pose", "2 minutes")
        )
    )
)

// --- NEW: Filter categories for the chips ---
val filterCategories = listOf("All", "Strength", "Cardio", "Flexibility")

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreenContent() {

    // --- State to hold the selected filter ---
    var selectedCategory by remember { mutableStateOf("All") }

    // --- State to hold the workout for the popup ---
    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }

    val filteredWorkouts = remember(selectedCategory) {
        if (selectedCategory == "All") {
            workoutPlans
        } else {
            workoutPlans.filter { it.category == selectedCategory }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- NEW: Filter Chip row ---
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filterCategories) { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        leadingIcon = {
                            if (category == selectedCategory) {
                                Icon(Icons.Default.Check, contentDescription = "Selected")
                            }
                        }
                    )
                }
            }
        }

        // --- NEW: Cards are now animated ---
        items(filteredWorkouts, key = { it.id }) { workout ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(animationSpec = tween(500), initialOffsetY = { it / 2 }),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                WorkoutCard(
                    workout = workout,
                    onClick = {selectedWorkout = workout}
                )
            }
        }
    }

    if (selectedWorkout != null) {
        WorkoutDetailDialog(
            workout = selectedWorkout!!,
            onDismiss = {selectedWorkout = null}
        )
    }
}

@Composable
fun WorkoutCard(
    workout: Workout,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(workout.imageUrl).crossfade(true).build(),
                contentDescription = workout.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Slightly taller for a more premium feel
            )
            // --- NEW: Gradient overlay for better text readability ---
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 400f // Start gradient lower down
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                // --- NEW: Row of tags for glanceable info ---
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkoutTag(
                        text = "${workout.durationInMinutes} min",
                        icon = Icons.Outlined.Timer
                    )
                    WorkoutTag(
                        text = workout.difficulty,
                        icon = Icons.Outlined.BarChart
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(workout.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Text(workout.description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
            }
            // Removed play icon until video playback enabled in future
//            Icon(
//                Icons.Filled.PlayCircleOutline,
//                "Play",
//                tint = Color.White.copy(alpha = 0.9f),
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(64.dp)
//            )
        }
    }
}

// Composable for the workout popup dialog
@Composable
fun WorkoutDetailDialog(
    workout: Workout,
    onDismiss: ()-> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                // Show the image at the top of the dialog
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(workout.imageUrl).crossfade(true).build(),
                    contentDescription = workout.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(workout.title, style = MaterialTheme.typography.headlineSmall)
            }
        },
        text = {
            // Use a LazyColumn in case the exercise list is very long
            LazyColumn {
                item {
                    Text(workout.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Exercises:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(workout.exercises) { (name, reps) ->
                    Text(
                        text = "• $name:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = reps,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

// --- Composable for the small info tags ---
@Composable
fun WorkoutTag(
    text: String,
    icon: ImageVector
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}