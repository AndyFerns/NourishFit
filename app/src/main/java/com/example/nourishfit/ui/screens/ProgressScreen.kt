package com.example.nourishfit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen() {
    var showChatbot by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Your Progress") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Ask AI Coach") },
                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "AI Coach") },
                onClick = { showChatbot = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Analytics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            item {
                ProgressChartCard(
                    title = "Weight Trend",
                    icon = Icons.Outlined.TrendingUp,
                    description = "Chart showing your weight over the last 30 days."
                )
            }
            item {
                ProgressChartCard(
                    title = "Calorie Intake",
                    icon = Icons.Outlined.BarChart,
                    description = "Chart showing your daily calorie intake vs. your goal."
                )
            }
        }

        if (showChatbot) {
            AiChatbotDialog(onDismiss = { showChatbot = false })
        }
    }
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

@Composable
fun AiChatbotDialog(onDismiss: () -> Unit) {
    // A very simple placeholder for a chatbot UI
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI Fitness Coach") },
        text = {
            Column {
                Text("This is where a conversational AI would help you with your fitness goals.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Ask a question...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
