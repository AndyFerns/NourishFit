package com.example.nourishfit.repository

import android.util.Log
import com.example.nourishfit.data.db.FoodDao
import com.example.nourishfit.data.db.FoodEntity
//import com.example.nourishfit.data.db.RunDao
//import com.example.nourishfit.data.db.RunEntity
import kotlinx.coroutines.flow.Flow
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

// --- FIX 1: The constructor now also accepts RunDao ---
class FoodRepository(private val foodDao: FoodDao) {
    // --- Get an instance of Cloud Firestore ---
    private val firestore = Firebase.firestore

    // --- Food functions ---
    fun getFoodsForDate(date: String, userId: String): Flow<List<FoodEntity>> = foodDao.getFoodsForDate(date, userId)

    suspend fun addFood(food: FoodEntity) {
        try {
            // 1. Insert into local Room database
            // The 'food' object has id=0 here
            val newRoomId = foodDao.insertFood(food)

            // 2. Create a new object with the real ID
            val foodWithId = food.copy(id = newRoomId.toInt())

            // 3. Save to Firestore
            firestore.collection("users")
                .document(foodWithId.userId)
                .collection("foodLogs")
                .document(newRoomId.toString()) // Use Room's ID as the Firestore ID
                .set(foodWithId)
                .await()
            Log.d("FirestoreSync", "Food item successfully synced.")

        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing food item", e)
            // If cloud fails, data is still saved locally.
        }
    }
    suspend fun updateFood(food: FoodEntity) = foodDao.updateFood(food)
    suspend fun deleteFood(food: FoodEntity) {
        try {
            // 1. Delete from local Room database
            foodDao.deleteFood(food)

            // 2. Delete from Firestore
            firestore.collection("users")
                .document(food.userId)
                .collection("foodLogs")
                .document(food.id.toString()) // Use the existing ID
                .delete()
                .await()
            Log.d("FirestoreSync", "Food item successfully deleted from sync.")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting synced food item", e)
        }
    }

    suspend fun getFoodCountForDate(date: String, userId: String) = foodDao.getFoodCountForDate(date, userId)

    // Removed run-related functions from FoodRepository
    // --- Add the missing functions for Runs --- //
    // TODO: refactor into RunRepository.kt

    // --- Run functions ---
//    suspend fun addRun(run: RunEntity) {
//        try {
//            // 1. Insert into local Room database
//            val newRoomId = runDao.insertRun(run)
//
//            // 2. Create object with real ID
//            val runWithId = run.copy(id = newRoomId.toInt())
//
//            // 3. Save to Firestore
//            firestore.collection("users")
//                .document(runWithId.userId)
//                .collection("runs")
//                .document(newRoomId.toString()) // Use Room's ID as the Firestore ID
//                .set(runWithId)
//                .await()
//            Log.d("FirestoreSync", "Run successfully synced.")
//        } catch (e: Exception) {
//            Log.e("FirestoreSync", "Error syncing run", e)
//        }
//    }

//    fun getAllRunsByUser(userId: String): Flow<List<RunEntity>> = runDao.getAllRunsByUser(userId)
}

