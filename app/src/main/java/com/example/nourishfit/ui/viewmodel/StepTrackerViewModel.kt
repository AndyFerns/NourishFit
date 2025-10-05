package com.example.nourishfit.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
// --- THE CHANGE: Import GeoPoint from OSMDroid ---
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TrackingState(
    val isTracking: Boolean = false,
    // --- THE CHANGE: The route is now a list of GeoPoints ---
    val route: List<GeoPoint> = emptyList(),
    val distance: Double = 0.0,
    val timeInMillis: Long = 0L
)

class StepTrackerViewModel : ViewModel() {

    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState = _trackingState.asStateFlow()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun startTracking(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(3000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // --- THE CHANGE: Create a GeoPoint object ---
                    val newPoint = GeoPoint(location.latitude, location.longitude)
                    _trackingState.value = _trackingState.value.copy(
                        route = _trackingState.value.route + newPoint
                    )
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
        _trackingState.value = _trackingState.value.copy(isTracking = true)
    }

    fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        _trackingState.value = _trackingState.value.copy(isTracking = false)
    }
}