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
        .fallbackToDestructiveMigration()
        .build()

        repository = FoodRepository(db.foodDao())
    }
}
