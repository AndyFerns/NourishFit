package com.example.nourishfit.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (locationPermissionsState.allPermissionsGranted) {
                OsmMapView(
                    route = trackingState.route,
                    isTracking = trackingState.isTracking
                )
            } else {
                PermissionRequestHandler(
                    onGrantPermissionClick = { locationPermissionsState.launchMultiplePermissionRequest() },
                    shouldShowRationale = locationPermissionsState.shouldShowRationale
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatDisplay(label = "Distance", value = "0.0 km")
                        StatDisplay(label = "Time", value = "00:00")
                        StatDisplay(label = "Pace", value = "0'00\"/km")
                    }
                }
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
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .size(72.dp)
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
fun OsmMapView(route: List<GeoPoint>, isTracking: Boolean) {
    val context = LocalContext.current
    val locationOverlay = remember { MyLocationNewOverlay(GpsMyLocationProvider(context), MapView(context)) }

    DisposableEffect(Unit) {
        locationOverlay.enableMyLocation()
        onDispose {
            locationOverlay.disableMyLocation()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                overlays.add(locationOverlay)
            }
        },
        update = { mapView ->
            mapView.overlays.removeIf { it is Polyline }

            if (route.size > 1) {
                val polyline = Polyline().apply {
                    setPoints(route)
                    color = 0xFF0000FF.toInt() // Blue color
                    width = 15f
                }
                mapView.overlays.add(polyline)
            }

            if (isTracking && route.isNotEmpty()) {
                mapView.controller.animateTo(route.last(), 17.0, 1000)
            } else if (!isTracking && route.isEmpty()) {
                val startPoint = GeoPoint(19.0441, 73.0242) // Navi Mumbai
                mapView.controller.setCenter(startPoint)
            }
            mapView.invalidate()
        }
    )
}

@Composable
fun PermissionRequestHandler(onGrantPermissionClick: () -> Unit, shouldShowRationale: Boolean) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val textToShow = if (shouldShowRationale) {
            "Tracking your run requires location access. Please grant the permission to continue."
        } else {
            "Location permission is required for this feature. Please enable it in settings."
        }

        Text(textToShow, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (!shouldShowRationale) {
                // This intent opens the app's specific settings page
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