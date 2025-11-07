package com.example.nourishfit.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.preference.PreferenceManager
import android.provider.Settings
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
import java.util.concurrent.TimeUnit
import com.example.nourishfit.ui.screens.formatDistance
import com.example.nourishfit.ui.screens.formatPace
import com.example.nourishfit.ui.screens.formatTime

// --- CHANGE: Renamed to 'StepTrackerScreenContent' ---
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StepTrackerScreenContent(viewModel: StepTrackerViewModel) {
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

    // --- THE CHANGE: Removed Scaffold ---
    Box(
        modifier = Modifier
            .fillMaxSize()
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
                    val formattedTime = formatTime(trackingState.timeInMillis)
                    val formattedDistance = formatDistance(trackingState.distanceMeters)
                    val formattedPace = formatPace(trackingState.distanceMeters, trackingState.timeInMillis)

                    StatDisplay(label = "Distance", value = formattedDistance)
                    StatDisplay(label = "Time", value = formattedTime)
                    StatDisplay(label = "Pace", value = formattedPace)
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

@Composable
fun OsmMapView(route: List<GeoPoint>, isTracking: Boolean) {
    val context = LocalContext.current
    val hasCenteredMap = remember { mutableStateOf(false) }

    val locationOverlay = remember {
        val provider = GpsMyLocationProvider(context)
        MyLocationNewOverlay(provider, MapView(context))
    }

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

                locationOverlay.runOnFirstFix {
                    post {
                        if (!hasCenteredMap.value) {
                            controller.animateTo(locationOverlay.myLocation, 17.0, 1500L)
                            hasCenteredMap.value = true
                        }
                    }
                }
            }
        },
        update = { mapView ->
            val polylines = mapView.overlays.filterIsInstance<Polyline>()
            mapView.overlays.removeAll(polylines)

            if (route.size > 1) {
                val polyline = Polyline().apply {
                    setPoints(route)
                    paint.color = 0xFF007BFF.toInt() // A nicer blue color
                    paint.strokeWidth = 15f
                }
                mapView.overlays.add(polyline)
            }

            if (isTracking && route.isNotEmpty()) {
                mapView.controller.animateTo(route.last(), 17.0, 1000)
            } else if (!isTracking && route.isEmpty() && !hasCenteredMap.value) {
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