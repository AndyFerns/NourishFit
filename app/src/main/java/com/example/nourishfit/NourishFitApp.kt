package com.example.nourishfit

import android.app.Application
import androidx.room.Room
import com.example.nourishfit.data.db.AppDatabase
import com.example.nourishfit.repository.FoodRepository
import com.google.firebase.Firebase // Firebase Deps
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore

class NourishFitApp : Application() {
    lateinit var repository: FoodRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // --- Enable Firestore offline persistence ---
        // This tells Firebase to automatically sync data when the network is back.
        val firestore = Firebase.firestore
        val settings = firestoreSettings {
            isPersistenceEnabled = true

            // --- Setup Room Database ---
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "nourishfit_db"
            )
                // --- FIX 3: Add this to handle the database version change (from 1 to 2) ---
                // This will delete the old database and create the new one.
                .fallbackToDestructiveMigration()
                .build()

            // --- FIX 4: Provide *both* DAOs to the repository's constructor ---
            repository = FoodRepository(db.foodDao(), db.runDao())
        }
    }
}

