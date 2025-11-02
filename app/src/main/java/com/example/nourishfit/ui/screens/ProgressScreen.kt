package com.example.nourishfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nourishfit.data.db.RunEntity
import com.example.nourishfit.ui.viewmodel.ProgressViewModel
import java.text.SimpleDateFormat
import java.util.*

// --- THE CHANGE: Renamed to '...Content' and removed Scaffold ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreenContent(
    viewModel: ProgressViewModel,
    onNavigateToChat: () -> Unit
) {
    var showChatbot by remember { mutableStateOf(false) }
    val allRuns by viewModel.allRuns.collectAsState()

    // --- THE CHANGE: Removed Scaffold, using Box for layout ---
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Analytics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            item {
                ProgressChartCard("Weight Trend", Icons.AutoMirrored.Outlined.TrendingUp, "Chart showing your weight over time.")
            }
            item {
                ProgressChartCard("Calorie Intake", Icons.Outlined.BarChart, "Chart showing daily calorie intake.")
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Activity History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            if (allRuns.isEmpty()) {
                item {
                    Text(
                        "No runs recorded yet. Go to the Step Tracker to start your first activity!",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(allRuns) { run ->
                    RunHistoryCard(run = run)
                }
            }
        }

        // --- THE CHANGE: FAB is now aligned within the Box ---
        ExtendedFloatingActionButton(
            text = { Text("Ask AI Coach") },
            icon = { Icon(Icons.AutoMirrored.Filled.Chat, "AI Coach") },
            onClick = onNavigateToChat,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )

        //if (showChatbot) { AiChatbotDialog(onDismiss = { showChatbot = false }) }
    }
}

@Composable
fun RunHistoryCard(run: RunEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(run.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // We can re-use the StatDisplay from StepTrackerScreen
                StatDisplay(label = "Distance", value = formatDistance(run.distanceMeters))
                StatDisplay(label = "Time", value = formatTime(run.timeInMillis))
                StatDisplay(label = "Pace", value = formatPace(run.distanceMeters, run.timeInMillis))
            }
        }
    }
}
// ... (The rest of your file is unchanged)
@Composable
fun AiChatbotDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI Fitness Coach (Chatbot)") },
        text = {
            Column {
                Text("A conversational AI helps you with your fitness goals.")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField("", {}, label = { Text("Ask a question...") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}
@Composable
fun ProgressChartCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
