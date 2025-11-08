package com.example.nourishfit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nourishfit.repository.ActivityRepository
import com.example.nourishfit.repository.FoodRepository

// consolidated all  factories into one file for better organization.

// Factory for DietTrackerScreen
class FoodViewModelFactory(private val repository: FoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: FoodViewModel")
    }
}

// Factory for Step Tracker Screen
class StepTrackerViewModelFactory(private val repository: ActivityRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StepTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StepTrackerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: StepTrackerViewModel")
    }
}

// Progress Screen Factory
class ProgressViewModelFactory(private val repository: ActivityRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgressViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ProgressViewModel")
    }
}
