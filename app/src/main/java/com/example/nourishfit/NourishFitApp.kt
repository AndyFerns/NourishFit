package com.example.nourishfit

import android.app.Application
import androidx.room.Room
import com.example.nourishfit.data.db.AppDatabase
import com.example.nourishfit.repository.ActivityRepository
import com.example.nourishfit.repository.FoodRepository
import com.google.firebase.Firebase // Firebase Deps
import com.google.firebase.firestore.firestore
//import com.google.firebase.firestore.firestore
//import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings

class NourishFitApp : Application() {
    lateinit var foodRepository: FoodRepository
        private set
    lateinit var activityRepository: ActivityRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // enable offline persistence
        Firebase.firestore.enableNetwork()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "nourishfit_db"
        )
            .fallbackToDestructiveMigration() // This handles the v3 -> v4 migration
            .build()

        // --- THE CHANGE: Initialize both repositories ---
        foodRepository = FoodRepository(db.foodDao())
        activityRepository = ActivityRepository(db.runDao(), db.weightDao())
    }
}
