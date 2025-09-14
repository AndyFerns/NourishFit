package com.example.nourishfit.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FoodEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration (from = 1, to = 2)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
}
