package com.example.nourishfit.repository

import android.util.Log
import com.example.nourishfit.data.db.RunDao
import com.example.nourishfit.data.db.RunEntity
import com.example.nourishfit.data.db.WeightDao
import com.example.nourishfit.data.db.WeightEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

// This new repository will handle runs and weight
class ActivityRepository(
    private val runDao: RunDao,
    private val weightDao: WeightDao
) {
    private val firestore = Firebase.firestore

    // --- Run Functions ---

    fun getAllRunsByUser(userId: String): Flow<List<RunEntity>> = runDao.getAllRunsByUser(userId)

    suspend fun addRun(run: RunEntity) {
        try {
            val newRoomId = runDao.insertRun(run)
            val runWithId = run.copy(id = newRoomId.toInt())

            firestore.collection("users")
                .document(runWithId.userId)
                .collection("runs")
                .document(newRoomId.toString())
                .set(runWithId)
                .await()
            Log.d("FirestoreSync", "Run successfully synced.")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing run", e)
        }
    }

    // --- Weight Functions ---

    fun getWeightHistory(userId: String): Flow<List<WeightEntity>> = weightDao.getWeightHistory(userId)

    suspend fun addWeight(weight: WeightEntity) {
        try {
            // Use the timestamp as the document ID to prevent multiple entries (or use date string)
            val docId = weight.timestamp.toString()

            // Save to local Room DB
            weightDao.insertWeight(weight)

            // Save to Firestore
            firestore.collection("users")
                .document(weight.userId)
                .collection("weightLog")
                .document(docId)
                .set(weight)
                .await()
            Log.d("FirestoreSync", "Weight successfully synced.")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing weight", e)
        }
    }
}