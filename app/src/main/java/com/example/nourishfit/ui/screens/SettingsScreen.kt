package com.example.nourishfit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// --- Renamed to '...Content' and removed Scaffold ---
@Composable
fun SettingsScreenContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item { SectionHeader("Account") }
        item { SettingsItem("Profile", Icons.Default.Person) }

        item { SectionHeader("Notifications (AI Alerts)") }
        item { NotificationToggle("Meal Reminders") }
        item { NotificationToggle("Weekly Progress Summary") }

        item { SectionHeader("About") }
        item { SettingsItem("Help & Support", Icons.AutoMirrored.Filled.HelpOutline) }
    }
}

// ... (The rest of your file is unchanged)
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun NotificationToggle(title: String) {
    var isChecked by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, title)
            Spacer(Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
        }
        Switch(checked = isChecked, onCheckedChange = { isChecked = it })
    }
}
