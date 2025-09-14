package com.example.nourishfit.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nourishfit.R // Make sure you have an image in your drawable folder
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(onStartTrackingClick: () -> Unit) {
    // get animation state
    var isAnimating by remember { mutableStateOf(false) }

    // get animation value
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.2f else 1.0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 300,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "Button Scale Animation"
    )

    // only run coroutine when isAnimating == true
    LaunchedEffect(key1 = isAnimating) {
        if (isAnimating) {
            // Wait for the animation to play (a little longer than the animation itself).
            delay(400L)
            // perform the navigation AFTER delay
            onStartTrackingClick()
            // Reset the state for the next time the user visits the screen.
            isAnimating = false
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // It's a good idea to have a nice logo or image here.
            // Make sure to add an image to your res/drawable folder.
            // I'm assuming you have one named `ic_launcher_foreground`.
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "NourishFit Logo",
                modifier = Modifier.size(150.dp)
            )
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
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    // --- 4. The Trigger ---
                    // The button click ONLY starts the animation.
                    // The LaunchedEffect will handle the navigation.
                    isAnimating = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    // --- 5. Apply the Animation ---
                    // The button's scale will now be driven by our animation value.
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                // The button is disabled while animating to prevent double-clicks.
                enabled = !isAnimating
            ) {
                Text("Start Tracking")
            }
        }
    }
}
