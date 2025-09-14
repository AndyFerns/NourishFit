package com.example.nourishfit.repository

import com.example.nourishfit.data.db.FoodDao
import com.example.nourishfit.data.db.FoodEntity
import kotlinx.coroutines.flow.Flow

class FoodRepository(private val dao: FoodDao) {
    // accept user ID and pass it along to the DAO
    fun getFoodsForDate(date: String, userID: String): Flow<List<FoodEntity>> = dao.getFoodsForDate(date, userID)
    suspend fun addFood(food: FoodEntity) = dao.insertFood(food)
    suspend fun updateFood(food: FoodEntity) = dao.updateFood(food)
    suspend fun deleteFood(food: FoodEntity) = dao.deleteFood(food)
}
