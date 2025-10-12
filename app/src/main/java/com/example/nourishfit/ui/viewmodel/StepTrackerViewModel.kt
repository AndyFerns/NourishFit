package com.example.nourishfit.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nourishfit.data.db.RunEntity
import com.example.nourishfit.repository.FoodRepository
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

// The TrackingState data class is unchanged
data class TrackingState(
    val isTracking: Boolean = false,
    val route: List<GeoPoint> = emptyList(),
    val distanceMeters: Double = 0.0,
    val timeInMillis: Long = 0L
)

// --- THE CHANGE: The ViewModel now requires the repository to save runs ---
class StepTrackerViewModel(private val repository: FoodRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState = _trackingState.asStateFlow()
    private var startTime: Long = 0L

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var timerJob: Job? = null

    @SuppressLint("MissingPermission")
    fun startTracking(context: Context) {
        startTime = System.currentTimeMillis() // Record the start time
        _trackingState.value = TrackingState(isTracking = true)
        // ... (rest of the startTracking function is the same)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(3000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val newPoint = GeoPoint(location.latitude, location.longitude)
                    val currentRoute = _trackingState.value.route
                    var newDistance = _trackingState.value.distanceMeters

                    if (currentRoute.isNotEmpty()) {
                        val lastPoint = currentRoute.last()
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            lastPoint.latitude, lastPoint.longitude,
                            newPoint.latitude, newPoint.longitude,
                            results
                        )
                        newDistance += results[0]
                    }

                    _trackingState.value = _trackingState.value.copy(
                        route = currentRoute + newPoint,
                        distanceMeters = newDistance
                    )
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())

        timerJob = viewModelScope.launch {
            while (_trackingState.value.isTracking) {
                delay(1000)
                _trackingState.value = _trackingState.value.copy(
                    timeInMillis = _trackingState.value.timeInMillis + 1000
                )
            }
        }
    }

    fun stopTracking() {
        timerJob?.cancel()
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        _trackingState.value = _trackingState.value.copy(isTracking = false)

        // --- NEW: Save the run to the database ---
        saveCompletedRun()
    }

    private fun saveCompletedRun() {
        viewModelScope.launch {
            val finalState = _trackingState.value
            val userId = auth.currentUser?.uid

            // Only save if the run has a valid user and some data
            if (userId != null && finalState.distanceMeters > 0 && finalState.timeInMillis > 0) {
                val run = RunEntity(
                    timestamp = startTime,
                    timeInMillis = finalState.timeInMillis,
                    distanceMeters = finalState.distanceMeters,
                    route = finalState.route,
                    userId = userId
                )
                repository.addRun(run)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }
}