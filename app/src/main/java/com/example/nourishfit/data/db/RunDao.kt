package com.example.nourishfit.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: RunEntity): Long

    // Gets all runs for a specific user, ordered from newest to oldest
    @Query("SELECT * FROM runs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllRunsByUser(userId: String): Flow<List<RunEntity>>
}
