package com.example.nourishfit.ui.screens

import java.util.concurrent.TimeUnit

// moved these functions here and removed 'private' so all screens can use them.

fun formatTime(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun formatDistance(meters: Double): String {
    val kilometers = meters / 1000.0
    return String.format("%.2f km", kilometers)
}

fun formatPace(meters: Double, millis: Long): String {
    if (meters < 1 || millis < 1000) {
        return "0'00\"/km"
    }
    val kilometers = meters / 1000.0
    val seconds = millis / 1000.0
    val secondsPerKm = seconds / kilometers
    val paceMinutes = (secondsPerKm / 60).toInt()
    val paceSeconds = (secondsPerKm % 60).toInt()
    return String.format("%d'%02d\"/km", paceMinutes, paceSeconds)
}
