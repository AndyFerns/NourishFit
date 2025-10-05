package com.example.nourishfit.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nourishfit.BuildConfig
import com.example.nourishfit.ui.viewmodel.StepTrackerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun StepTrackerScreen(viewModel: StepTrackerViewModel = viewModel()) {
    val context = LocalContext.current
    val trackingState by viewModel.trackingState.collectAsState()

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Activity Tracker") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (locationPermissionsState.allPermissionsGranted) {
                    OsmMapView(route = trackingState.route)
                } else {
                    // --- THE CHANGE: This UI now handles all permission states ---
                    PermissionRequestHandler(
                        onGrantPermissionClick = { locationPermissionsState.launchMultiplePermissionRequest() },
                        shouldShowRationale = locationPermissionsState.shouldShowRationale
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                    onClick = {
                        if (locationPermissionsState.allPermissionsGranted) {
                            if (trackingState.isTracking) {
                                viewModel.stopTracking()
                            } else {
                                viewModel.startTracking(context)
                            }
                        } else {
                            locationPermissionsState.launchMultiplePermissionRequest()
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (trackingState.isTracking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (trackingState.isTracking) "Stop Tracking" else "Start Tracking",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

// --- NEW HELPER COMPOSABLE for handling permission UI ---
@Composable
fun PermissionRequestHandler(
    onGrantPermissionClick: () -> Unit,
    shouldShowRationale: Boolean
) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // This text changes based on whether the user has permanently denied the permission
        val textToShow = if (shouldShowRationale) {
            // This shows if the user has denied the permission in the past.
            "Tracking your run requires location access. Please grant the permission to continue."
        } else {
            // This shows on first launch and after permanent denial.
            "Location permission is required for this feature."
        }

        Text(textToShow, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            // If rationale should be shown, the button just asks again.
            // If it's permanently denied, this will open the app settings.
            if (!shouldShowRationale) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            } else {
                onGrantPermissionClick()
            }
        }) {
            val buttonText = if (!shouldShowRationale) "Open Settings" else "Grant Permission"
            Text(buttonText)
        }
    }
}


@Composable
fun OsmMapView(route: List<GeoPoint>) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            if (route.size > 1) {
                val polyline = Polyline().apply {
                    setPoints(route)
                    color = android.graphics.Color.BLUE
                    width = 15f
                }
                mapView.overlays.add(polyline)

                route.lastOrNull()?.let {
                    mapView.controller.animateTo(it, 17.0, 1000)
                }
            } else {
                val startPoint = GeoPoint(19.0760, 72.8777) // Mumbai
                mapView.controller.setCenter(startPoint)
            }
            mapView.invalidate()
        }
    )
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