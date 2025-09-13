package com.example.nourishfit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// This sealed interface will represent the state of our UI
sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Email and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "User not found. Please sign up."
                    is FirebaseAuthInvalidCredentialsException -> "Invalid password. Please try again."
                    else -> "An unknown error occurred."
                }
                _uiState.value = LoginUiState.Error(errorMessage)
            }
        }
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Email and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Sign up failed.")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
