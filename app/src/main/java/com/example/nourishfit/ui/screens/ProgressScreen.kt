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
import androidx.compose.ui.unit.dp
import com.example.nourishfit.data.db.RunEntity
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
    val allRuns by viewModel.allRuns.collectAsState()

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
                // --- THE CHANGE: Swapped text for a placeholder line chart ---
                ProgressChartCard(
                    title = "Weight Trend",
                    icon = Icons.AutoMirrored.Outlined.TrendingUp
                ) {
                    PlaceholderLineChart()
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

// --- NEW: A simple, animated placeholder line chart ---
@Composable
fun PlaceholderLineChart() {
    val points = remember { listOf(0.4f, 0.3f, 0.5f, 0.4f, 0.6f, 0.8f, 0.7f) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val pathGradient = Path()

        val xStep = size.width / (points.size - 1)
        val yMax = size.height

        points.forEachIndexed { index, point ->
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

