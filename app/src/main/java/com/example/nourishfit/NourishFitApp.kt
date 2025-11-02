package com.example.nourishfit

import android.app.Application
import androidx.room.Room
import com.example.nourishfit.data.db.AppDatabase
import com.example.nourishfit.repository.FoodRepository

class NourishFitApp : Application() {
    lateinit var repository: FoodRepository
        private set

    override fun onCreate() {
        super.onCreate()

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

