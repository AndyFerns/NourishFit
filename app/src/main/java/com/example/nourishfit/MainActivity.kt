package com.example.nourishfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.nourishfit.navigation.AppNavigation
import com.example.nourishfit.ui.theme.NourishFitTheme
import com.example.nourishfit.ui.viewmodel.FoodViewModelFactory
import com.example.nourishfit.ui.viewmodel.ProgressViewModelFactory
import com.example.nourishfit.ui.viewmodel.StepTrackerViewModelFactory
//import com.example.nourishfit.ui.screens.DietTrackerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // getter for repo from single app interface
        val repository = (application as NourishFitApp).repository

        // creating ViewModelFactory needed by the screens
        val foodViewModelFactory = FoodViewModelFactory(repository)
        val stepTrackerViewModelFactory = StepTrackerViewModelFactory(repository)
        val progressViewModelFactory = ProgressViewModelFactory(repository)

        enableEdgeToEdge()
        setContent {
            NourishFitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use the single navigation graph
                    AppNavigation(
                        foodViewModelFactory = foodViewModelFactory,
                        stepTrackerViewModelFactory = stepTrackerViewModelFactory,
                        progressViewModelFactory = progressViewModelFactory
                    )
                }
            }
        }
    }
}
