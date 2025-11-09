package com.example.nourishfit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nourishfit.R
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onStartTrackingClick: () -> Unit,
    // --- NEW: Add a parameter for the login button ---
    onLoginClick: () -> Unit
) {
    var isButtonAnimating by remember { mutableStateOf(false) }

    // --- NEW: State for the entrance animation ---
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Trigger the entrance animation when the screen first loads
        contentVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isButtonAnimating) 1.2f else 1.0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 300,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "Button Scale Animation"
    )

    LaunchedEffect(key1 = isButtonAnimating) {
        if (isButtonAnimating) {
            delay(400L)
            onStartTrackingClick()
            isButtonAnimating = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- NEW: Content is wrapped in a weighted, centered Box ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Call the animated intro function here
                AnimatedIntro(contentVisible)
            }

            // --- This Column holds the buttons at the bottom ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { isButtonAnimating = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    enabled = !isButtonAnimating
                ) {
                    Text("Start Tracking")
                }

                // --- NEW: "Log In" button for existing users ---
                TextButton(onClick = onLoginClick) {
                    Text("Already have an account? Log In")
                }
            }
        }
    }
}

@Composable
private fun AnimatedIntro(contentVisible: Boolean) {
    // --- NEW: Entrance animation ---
    AnimatedVisibility(
        visible = contentVisible,
        enter = fadeIn(animationSpec = tween(1000)) +
                slideInVertically(animationSpec = tween(1000), initialOffsetY = { it / 2 })
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // --- FIX: Logo is now in a circular Surface ---
            Surface(
                modifier = Modifier.size(150.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 8.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "NourishFit Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp) // Add padding so the logo fits inside
                )
            }
            // --- END OF FIX ---

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to NourishFit",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Begin your healthier lifestyle now",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}