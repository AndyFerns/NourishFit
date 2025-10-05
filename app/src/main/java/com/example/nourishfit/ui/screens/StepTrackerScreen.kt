package com.example.nourishfit.ui.screens

import android.Manifest
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    // Set up OSMDroid configuration
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
                    OsmMapView(
                        route = trackingState.route
                    )
                } else {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Location permission is required to track your run.")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                            Text("Grant Permission")
                        }
                    }
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

@Composable
fun OsmMapView(route: List<GeoPoint>) {
    val context = LocalContext.current

    // Using AndroidView to host the classic MapView
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
            // Clear old overlays
            mapView.overlays.clear()

            // Add new route polyline
            if (route.size > 1) {
                val polyline = Polyline().apply {
                    setPoints(route)
                    color = android.graphics.Color.BLUE
                    width = 15f
                }
                mapView.overlays.add(polyline)

                // Animate to the latest point
                route.lastOrNull()?.let {
                    mapView.controller.animateTo(it, 17.0, 1000)
                }
            } else {
                // Set a default start location if the route is empty
                val startPoint = GeoPoint(19.0760, 72.8777) // Mumbai
                mapView.controller.setCenter(startPoint)
            }
            // Invalidate to redraw the map
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