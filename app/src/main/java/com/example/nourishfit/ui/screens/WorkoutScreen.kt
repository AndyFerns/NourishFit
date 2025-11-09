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

// --- NEW: Added more glanceable info to the data class ---
data class Workout(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val durationInMinutes: Int,
    val difficulty: String,
    val category: String // For filtering
)

val workoutPlans = listOf(
    Workout(1, "Full Body Strength", "Comprehensive workout for all muscle groups.", "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg?auto=compress&cs=tinysrgb&w=600", 45, "Intermediate", "Strength"), // Person lifting weights, clear and direct
    Workout(2, "Cardio Blast", "High-intensity interval training.", "https://images.pexels.com/photos/2294361/pexels-photo-2294361.jpeg?auto=compress&cs=tinysrgb&w=600", 30, "Hard", "Cardio"), // Dynamic running shot
    Workout(3, "Upper Body Focus", "Target your chest, back, and arms.", "https://images.pexels.com/photos/7031705/pexels-photo-7031705.jpeg?auto=compress&cs=tinysrgb&w=600", 40, "Intermediate", "Strength"), // Person doing push-ups or bench press
    Workout(4, "Core & Flexibility", "Improve your core strength and flexibility.", "https://images.pexels.com/photos/3822622/pexels-photo-3822622.jpeg?auto=compress&cs=tinysrgb&w=600", 25, "Beginner", "Flexibility") // Person stretching or doing yoga
)

// --- NEW: Filter categories for the chips ---
val filterCategories = listOf("All", "Strength", "Cardio", "Flexibility")

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreenContent() {

    // --- NEW: State to hold the selected filter ---
    var selectedCategory by remember { mutableStateOf("All") }

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
                WorkoutCard(workout)
            }
        }
    }
}

@Composable
fun WorkoutCard(workout: Workout) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = { /* TODO */ }
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
            Icon(
                Icons.Filled.PlayCircleOutline,
                "Play",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
            )
        }
    }
}

// --- NEW: Composable for the small info tags ---
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