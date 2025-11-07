package com.example.nourishfit.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    // We use REPLACE so that if a user logs their weight multiple times in one day,
    // we only store the most recent one (this assumes we use a date-based ID)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: WeightEntity)

    // Gets all weight entries for a user, ordered by date
    @Query("SELECT * FROM weight_log WHERE userId = :userId ORDER BY timestamp ASC")
    fun getWeightHistory(userId: String): Flow<List<WeightEntity>>
}