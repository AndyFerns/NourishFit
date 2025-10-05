package com.example.nourishfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepTrackerScreen() {
    // This state would be driven by a ViewModel in a real implementation
    var isTracking by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Activity Tracker") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder for the Google Map view
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = "Map Placeholder",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Google Map would be displayed here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stats and Control Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatDisplay(label = "Distance", value = "0.0 km")
                    StatDisplay(label = "Time", value = "00:00")
                    StatDisplay(label = "Pace", value = "0'00\"/km")
                }
                Spacer(modifier = Modifier.height(24.dp))
                LargeFloatingActionButton(
                    onClick = { isTracking = !isTracking },
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (isTracking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (isTracking) "Stop Tracking" else "Start Tracking",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatDisplay(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
