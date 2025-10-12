package com.example.nourishfit.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.osmdroid.util.GeoPoint

@Entity(tableName = "runs")
data class RunEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long, // When the run started
    val timeInMillis: Long,
    val distanceMeters: Double,
    val route: List<GeoPoint>, // The list of GPS points
    val userId: String
)
