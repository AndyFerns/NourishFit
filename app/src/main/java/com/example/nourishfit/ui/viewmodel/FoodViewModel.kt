package com.example.nourishfit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nourishfit.data.db.FoodEntity
import com.example.nourishfit.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FoodViewModel(private val repository: FoodRepository) : ViewModel() {

    // A formatter for converting LocalDate to the String format your DB expects.
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // **THE FIX**: Hold the current date in a MutableStateFlow.
    // This allows the UI to change it.
    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate

    // **THE FIX**: The foods list now reactively updates whenever _currentDate changes.
    // `flatMapLatest` is a powerful tool that switches to the new date's data flow.
    val foods: StateFlow<List<FoodEntity>> = _currentDate.flatMapLatest { date ->
        repository.getFoodsForDate(date.format(dateFormatter))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Public function for the UI to call to change the date.
    fun changeDate(newDate: LocalDate) {
        _currentDate.value = newDate
    }

    fun addFood(name: String, calories: Int) {
        viewModelScope.launch {
            val food = FoodEntity(
                name = name,
                calories = calories,
                date = _currentDate.value.format(dateFormatter) // Use the *current* date
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
class FoodViewModelFactory(private val repository: FoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
