package com.example.nourishfit.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calories: Int,
    // --- Macroes ---
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    // ---
    val date: String,
    val userId: String
)

