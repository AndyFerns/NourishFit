package com.example.nourishfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nourishfit.R

// Dummy data for workout plans
data class Workout(val id: Int, val title: String, val description: String, val imageUrl: String)

val workoutPlans = listOf(
    Workout(1, "Full Body Strength", "A comprehensive workout for all major muscle groups.", "https://placehold.co/600x400/222/FFF?text=Workout+1"),
    Workout(2, "Cardio Blast", "High-intensity interval training to boost your heart rate.", "https://placehold.co/600x400/333/FFF?text=Workout+2"),
    Workout(3, "Upper Body Focus", "Target your chest, back, shoulders, and arms.", "https://placehold.co/600x400/444/FFF?text=Workout+3"),
    Workout(4, "Core & Flexibility", "Improve your core strength and flexibility with yoga and pilates.", "https://placehold.co/600x400/555/FFF?text=Workout+4")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Workout Plans") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(workoutPlans) { workout ->
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
        onClick = { /* TODO: Navigate to workout details */ }
    ) {
        Box {
            // Placeholder for a video or image thumbnail (Multimedia)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(workout.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = workout.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = workout.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = workout.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Filled.PlayCircleOutline,
                contentDescription = "Play Workout",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
            )
        }
    }
}
