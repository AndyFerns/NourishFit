package com.example.nourishfit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nourishfit.data.db.RunEntity
import com.example.nourishfit.repository.FoodRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn

// This new ViewModel will be responsible for providing data to the ProgressScreen
class ProgressViewModel(repository: FoodRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // A flow that provides a list of all saved runs for the current user
    val allRuns: StateFlow<List<RunEntity>> = auth.currentUser?.uid?.let { userId ->
        repository.getAllRunsByUser(userId)
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()) ?: emptyFlow<List<RunEntity>>()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
