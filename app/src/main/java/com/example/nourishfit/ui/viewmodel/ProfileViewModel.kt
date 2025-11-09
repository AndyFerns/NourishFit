package com.example.nourishfit.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

// Data class to hold the user's profile information
data class ProfileData(
    val name: String = "",
    val age: String = "",
    val weight: String = "", // Stored as String for the text field
    val height: String = "", // Stored as String for the text field
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    val photoUrl: String = ""
) {
    enum class ActivityLevel(val displayName: String, val multiplier: Double) {
        SEDENTARY("Sedentary", 1.2),
        LIGHT("Light", 1.375),
        MODERATE("Moderate", 1.55),
        VERY("Very Active", 1.725)
    }
}

// Sealed interface to represent the UI state
sealed interface ProfileUiState {
    data class Success(
        val profile: ProfileData,
        val tdee: Int = 2000,
        val isLoading: Boolean = false,
        val error: String? = null,
        val isSaveSuccessful: Boolean = false
    ) : ProfileUiState
    object Loading : ProfileUiState
}

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // The main UI state flow
    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileState = _profileState.asStateFlow()

    init {
        // Load the profile as soon as the ViewModel is created
        loadProfile()
    }

    private fun loadProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _profileState.value = ProfileUiState.Success(ProfileData(), error = "User not found.")
            return
        }

        viewModelScope.launch {
            try {
                val document = firestore.collection("users").document(userId).collection("profile").document("data").get().await()
                val profile = document.toObject<ProfileData>() ?: ProfileData(name = auth.currentUser?.displayName ?: "")
                _profileState.value = ProfileUiState.Success(profile = profile, tdee = calculateTdee(profile))
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile", e)
                _profileState.value = ProfileUiState.Success(ProfileData(), error = "Could not load profile.")
            }
        }
    }

    // Called by the UI when any text field changes
    fun updateProfile(newProfileData: ProfileData) {
        if (_profileState.value is ProfileUiState.Success) {
            _profileState.value = (_profileState.value as ProfileUiState.Success).copy(
                profile = newProfileData,
                tdee = calculateTdee(newProfileData),
                error = null,
                isSaveSuccessful = false
            )
        }
    }

    fun saveProfile() {
        val currentState = _profileState.value
        if (currentState !is ProfileUiState.Success) return

        val profile = currentState.profile
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _profileState.value = currentState.copy(error = "You must be logged in to save.")
            return
        }

        // Validate data before saving
        if (profile.name.isBlank() || profile.age.isBlank() || profile.weight.isBlank() || profile.height.isBlank()) {
            _profileState.value = currentState.copy(error = "All fields must be filled.")
            return
        }

        _profileState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId).collection("profile").document("data").set(profile).await()
                _profileState.value = currentState.copy(isLoading = false, isSaveSuccessful = true)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving profile", e)
                _profileState.value = currentState.copy(isLoading = false, error = "Failed to save profile.")
            }
        }
    }

    // This is the "AI" logic to calculate the user's calorie goal (TDEE)
    // We use the Harris-Benedict equation (a common standard)
    private fun calculateTdee(profile: ProfileData): Int {
        val age = profile.age.toIntOrNull() ?: 0
        val weight = profile.weight.toDoubleOrNull() ?: 0.0
        val height = profile.height.toDoubleOrNull() ?: 0.0
        val activityMultiplier = profile.activityLevel.multiplier

        if (age == 0 || weight == 0.0 || height == 0.0) {
            return 2000 // Return a default value if info is missing
        }

        // Using Harris-Benedict BMR calculation (assuming male for this example, a real app would check 'sex')
        // A real app would have a "Sex" field (Male/Female)
        val bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)

        val tdee = bmr * activityMultiplier
        return tdee.roundToInt()
    }

    // Resets the save success flag so the snackbar doesn't re-show
    fun resetSaveSuccess() {
        if (_profileState.value is ProfileUiState.Success) {
            _profileState.value = (_profileState.value as ProfileUiState.Success).copy(isSaveSuccessful = false)
        }
    }
}