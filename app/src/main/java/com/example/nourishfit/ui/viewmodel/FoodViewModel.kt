package com.example.nourishfit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nourishfit.data.db.FoodEntity
import com.example.nourishfit.repository.FoodRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class FoodViewModel(private val repository: FoodRepository) : ViewModel() {

    // A formatter for converting LocalDate to the String format your DB expects.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // user flow block to listen to authentication changes
    private val userFlow: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener {
            firebaseAuth -> trySend(firebaseAuth.currentUser).isSuccess
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    // hold the current date in a MutableStateFlow.
    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate

    // `flatMapLatest` is a powerful tool that switches to the new date's data flow.
    val foods: StateFlow<List<FoodEntity>> = combine(
        _currentDate,   // get the current date
        userFlow   // get the curr userID
    ) { date, user ->
        Pair(date, user?.uid)
    }.flatMapLatest { (date, userID) ->
        if (userID != null) {
            // if a user is logged in, fetch their food details for the current date
            repository.getFoodsForDate(date.format(dateFormatter), userID)
        } else {
            // if no user is logged in, provide an empty list
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Public function for the UI to call to change the date.
    fun changeDate(newDate: LocalDate) {
        _currentDate.value = newDate
    }

    fun addFood(name: String, calories: Int) {
        // get the curr userID
        val userID = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val food = FoodEntity(
                name = name,
                calories = calories,
                date = _currentDate.value.format(dateFormatter), // Use the *current* date
                userID = userID
            )
            repository.addFood(food)
        }
    }

    fun updateFood(food: FoodEntity) {
        viewModelScope.launch { repository.updateFood(food) }
    }

    fun deleteFood(food: FoodEntity) {
        viewModelScope.launch { repository.deleteFood(food) }
    }
}

// A Factory is needed to tell the system how to create our ViewModel,
// since it now requires a Repository in its constructor.
//class FoodViewModelFactory(private val repository: FoodRepository) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return FoodViewModel(repository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
