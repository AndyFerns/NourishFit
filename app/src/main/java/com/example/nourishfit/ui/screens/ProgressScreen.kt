package com.example.nourishfit.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nourishfit.data.db.RunEntity
import com.example.nourishfit.data.db.WeightEntity
import com.example.nourishfit.ui.viewmodel.ProgressViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreenContent(
    viewModel: ProgressViewModel,
    onNavigateToChat: () -> Unit
) {
    var showChatbot by remember { mutableStateOf(false) }

    // Data now comes from the Room-backed ViewModel
    val weightHistory by viewModel.weightHistory.collectAsState()
    val allRuns by viewModel.allRuns.collectAsState()

    var showAddWeightDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Analytics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                // Button to open the "Add Weight" dialog
                FilledTonalButton(onClick = { showAddWeightDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Weight", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Weight")
                }
            }
            item {
                // --- THE CHANGE: Swapped text for a placeholder line chart ---
                ProgressChartCard(
                    title = "Weight Trend",
                    icon = Icons.AutoMirrored.Outlined.TrendingUp
                ) {
                    // Pass the real weight data (from Room) to the chart
                    if (weightHistory.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Log your weight to see your trend!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        val weights = weightHistory.map { it.weight.toFloat() }
                        PlaceholderLineChart(points = weights)
                    }
                }
            }
            item {
                // --- THE CHANGE: Swapped text for a placeholder bar chart ---
                ProgressChartCard(
                    title = "Calorie Intake",
                    icon = Icons.Outlined.BarChart
                ) {
                    PlaceholderBarChart()
                }
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

        ExtendedFloatingActionButton(
            text = { Text("Ask AI Coach") },
            icon = { Icon(Icons.AutoMirrored.Filled.Chat, "AI Coach") },
            onClick = onNavigateToChat,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )

        // The dialog is now correctly handled by the navigation to ChatScreen
        // if (showChatbot) { AiChatbotDialog(onDismiss = { showChatbot = false }) }

        if (showAddWeightDialog) {
            AddWeightDialog(
                onDismiss = { showAddWeightDialog = false },
                onAddWeight = { weight ->
                    viewModel.addWeight(weight)
                    showAddWeightDialog = false
                }
            )
        }
    }
}


// Dialog for logging a new weight entry
@Composable
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onAddWeight: (Double) -> Unit
) {
    var weight by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Today's Weight") },
        text = {
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                suffix = { Text("kg") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = {
                val weightDouble = weight.toDoubleOrNull()
                if (weightDouble != null && weightDouble > 0) {
                    onAddWeight(weightDouble)
                    onDismiss() // Close dialog on success
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
                StatDisplay(label = "Distance", value = formatDistance(run.distanceMeters))
                StatDisplay(label = "Time", value = formatTime(run.timeInMillis))
                StatDisplay(label = "Pace", value = formatPace(run.distanceMeters, run.timeInMillis))
            }
        }
    }
}
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

// --- THE CHANGE: This Composable now accepts a 'content' lambda ---
@Composable
fun ProgressChartCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit // This will be our chart
) {
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
            // --- THE CHANGE: We render the chart content here ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

// This chart now accepts real data
@Composable
fun PlaceholderLineChart(points: List<Float>) {
    // Find min and max to normalize the data for drawing
    val min = points.minOrNull() ?: 0f
    val max = points.maxOrNull() ?: 1f

    val normalizedPoints = remember(points) {
        if (max == min) {
            // If all values are the same, just draw a flat line
            points.map { 0.5f }
        } else {
            // Normalize all points between 0.0 (min) and 1.0 (max)
            points.map { (it - min) / (max - min) }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        // Animate whenever the points list changes
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val pathGradient = Path()

        val xStep = size.width / (normalizedPoints.size - 1).coerceAtLeast(1)
        val yMax = size.height

        normalizedPoints.forEachIndexed { index, point ->
            // Invert Y axis for canvas (0,0 is top-left, we want 0,0 at bottom-left)
            val x = index * xStep
            val y = yMax - (point * yMax * animationProgress.value)

            if (index == 0) {
                path.moveTo(x, y)
                pathGradient.moveTo(x, y)
            } else {
                path.lineTo(x, y)
                pathGradient.lineTo(x, y)
            }

            drawCircle(color = primaryColor, radius = 8f, center = Offset(x, y))
        }

        // Draw the gradient shadow under the line
        pathGradient.lineTo(size.width, size.height)
        pathGradient.lineTo(0f, size.height)
        pathGradient.close()

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )

        drawPath(
            path = pathGradient,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent),
                startY = 0f,
                endY = size.height
            )
        )
    }
}

// --- NEW: A simple, animated placeholder bar chart ---
@Composable
fun PlaceholderBarChart() {
    val data = remember { listOf(0.8f, 0.5f, 0.7f, 0.4f, 0.6f, 0.9f, 0.5f) }
    val barColor = MaterialTheme.colorScheme.secondary

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { value ->
            val animationProgress = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                animationProgress.animateTo(value, animationSpec = tween(1000))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(animationProgress.value)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(barColor)
            )
        }
    }
}

