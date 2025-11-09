package com.example.nourishfit.ui.screens

import android.Manifest
import android.os.Build
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.nourishfit.workers.NotificationWorker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.TimeUnit

@Composable
fun SettingsScreenContent(
    onNavigateToProfile: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item { SectionHeader("Account") }
        item { SettingsItem(
            "Profile",
            Icons.Default.Person,
            onClick = onNavigateToProfile
        ) }

        item { SectionHeader("Notifications (AI Alerts)") }
        item { DailyReminderToggle() }
        item { NotificationToggle(title = "Weekly Progress Summary") } // This is still a placeholder

        item { SectionHeader("About") }
        item { SettingsItem("Help & Support", Icons.AutoMirrored.Filled.HelpOutline) }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DailyReminderToggle() {
    // --- FIX: 'LocalContext' is now resolved ---
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    val workName = "DailyFoodLogReminder"

    var isChecked by remember { mutableStateOf(false) }

    val postNotificationPermission =
        // --- FIX: 'Build' is now resolved ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // --- FIX: 'Manifest' is now resolved ---
            rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
        } else {
            null
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isChecked = !isChecked }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = "Meal Reminders")
            Spacer(modifier = Modifier.width(16.dp))
            Text("Daily Meal Reminders", style = MaterialTheme.typography.bodyLarge)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { checked ->
                isChecked = checked
                if (checked) {
                    if (postNotificationPermission == null || postNotificationPermission.status.isGranted) {
                        val dailyCheck = PeriodicWorkRequestBuilder<NotificationWorker>(
                            1, TimeUnit.DAYS
                        ).build()

                        workManager.enqueueUniquePeriodicWork(
                            workName,
                            ExistingPeriodicWorkPolicy.KEEP,
                            dailyCheck
                        )
                    } else {
                        postNotificationPermission.launchPermissionRequest()
                        isChecked = false
                    }
                } else {
                    workManager.cancelUniqueWork(workName)
                }
            }
        )
    }
}

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
    var isChecked by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, title)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
        }
        Switch(checked = isChecked, onCheckedChange = { isChecked = it })
    }
}
