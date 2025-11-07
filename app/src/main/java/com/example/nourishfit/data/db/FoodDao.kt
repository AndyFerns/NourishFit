package com.example.nourishfit.data.db

import androidx.room.*
import androidx.room.Dao
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity)

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Delete
    suspend fun deleteFood(food: FoodEntity)

    @Query("SELECT * FROM foods WHERE date = :date AND userID = :userID ORDER BY id DESC")
    fun getFoodsForDate(date: String, userID: String): Flow<List<FoodEntity>>

    // This will quickly check if any food exists for a user on a specific date.
    @Query("SELECT COUNT(*) FROM foods WHERE date = :date AND userId = :userId")
    suspend fun getFoodCountForDate(date: String, userId: String): Int
}
