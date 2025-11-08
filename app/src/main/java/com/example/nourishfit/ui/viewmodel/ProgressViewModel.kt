package com.example.nourishfit.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nourishfit.data.db.RunEntity
import com.example.nourishfit.repository.FoodRepository
import com.example.nourishfit.data.db.WeightEntity
import com.example.nourishfit.repository.ActivityRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

// Data class for Weight logging
data class WeightEntry(
    val timestamp: Long = 0L,
    val weight: Double = 0.0
)

// This new ViewModel will be responsible for providing data to the ProgressScreen
class ProgressViewModel(private val repository: ActivityRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    // --- THE CHANGE: Now reads from the repository (Room) ---
    val allRuns: StateFlow<List<RunEntity>> = userId?.let {
        repository.getAllRunsByUser(it)
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: emptyFlow<List<RunEntity>>().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- THE CHANGE: Now reads from the repository (Room) ---
    val weightHistory: StateFlow<List<WeightEntity>> = userId?.let {
        repository.getWeightHistory(it)
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: emptyFlow<List<WeightEntity>>().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWeight(weight: Double) {
        val userId = auth.currentUser?.uid ?: return

        // Use the start of the day as the timestamp to ensure one entry per day
        val timestamp = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

        val entry = WeightEntity(
            timestamp = timestamp,
            weight = weight,
            userId = userId
        )

        viewModelScope.launch {
            try {
                repository.addWeight(entry)
                Log.d("ProgressViewModel", "Weight added successfully")
            } catch (e: Exception) {
                Log.e("ProgressViewModel", "Error adding weight", e)
            }
        }
    }
}
