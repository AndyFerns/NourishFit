package com.example.nourishfit.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calories: Int,
    val date: String // format: yyyy-MM-dd
)
