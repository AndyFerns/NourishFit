package com.example.nourishfit.repository

import com.example.nourishfit.data.db.FoodDao
import com.example.nourishfit.data.db.FoodEntity
import com.example.nourishfit.data.db.RunDao
import com.example.nourishfit.data.db.RunEntity
import kotlinx.coroutines.flow.Flow

// --- FIX 1: The constructor now also accepts RunDao ---
class FoodRepository(private val foodDao: FoodDao, private val runDao: RunDao) {
    // Food functions (unchanged)
    fun getFoodsForDate(date: String, userId: String): Flow<List<FoodEntity>> = foodDao.getFoodsForDate(date, userId)
    suspend fun addFood(food: FoodEntity) = foodDao.insertFood(food)
    suspend fun updateFood(food: FoodEntity) = foodDao.updateFood(food)
    suspend fun deleteFood(food: FoodEntity) = foodDao.deleteFood(food)

    // --- Add the missing functions for Runs ---   //
    // TODO: refactor into RunRepository.kt
    suspend fun addRun(run: RunEntity) = runDao.insertRun(run)
    fun getAllRunsByUser(userId: String): Flow<List<RunEntity>> = runDao.getAllRunsByUser(userId)
}

