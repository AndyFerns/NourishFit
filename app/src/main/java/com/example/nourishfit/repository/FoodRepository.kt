package com.example.nourishfit.repository

import com.example.nourishfit.data.db.FoodDao
import com.example.nourishfit.data.db.FoodEntity
import kotlinx.coroutines.flow.Flow

class FoodRepository(private val dao: FoodDao) {
    fun getFoodsForDate(date: String): Flow<List<FoodEntity>> = dao.getFoodsForDate(date)
    suspend fun addFood(food: FoodEntity) = dao.insertFood(food)
    suspend fun updateFood(food: FoodEntity) = dao.updateFood(food)
    suspend fun deleteFood(food: FoodEntity) = dao.deleteFood(food)
}
