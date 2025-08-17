package com.example.nourishfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.nourishfit.navigation.AppNavigation
import com.example.nourishfit.ui.theme.NourishFitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NourishFitTheme {
                AppNavigation()
            }
        }
    }
}
