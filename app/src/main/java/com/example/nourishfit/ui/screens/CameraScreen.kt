package com.example.nourishfit.ui.screens

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onFoodScanned: (String) -> Unit // Callback to return the food name
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Camera Permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // 2. Camera Controller
    val cameraController = remember { LifecycleCameraController(context) }

    // 3. Image Labeler
    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    var isAnalyzing by remember { mutableStateOf(false) }

    // Function to analyze the image
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun analyzeImage(imageProxy: ImageProxy) {
        if (isAnalyzing) return
        isAnalyzing = true

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            labeler.process(image)
                .addOnSuccessListener { labels ->
                    // Find the best, most confident label
                    val bestLabel = labels.maxByOrNull { it.confidence }?.text ?: "Food"
                    Log.d("ML_SCANNER", "Found label: $bestLabel")
                    // Send the result back and close the screen
                    onFoodScanned(bestLabel)
                }
                .addOnFailureListener { e ->
                    Log.e("ML_SCANNER", "Failed to analyze image", e)
                    onFoodScanned("Food") // Send a default on failure
                }
                .addOnCompleteListener {
                    imageProxy.close() // VERY important to close the image
                }
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            // Camera Preview
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        this.controller = cameraController
                        cameraController.bindToLifecycle(lifecycleOwner)
                        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Capture Button
            Button(
                onClick = {
                    // Set up the analyzer
                    cameraController.setImageAnalysisAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        ImageAnalysis.Analyzer { imageProxy ->
                            analyzeImage(imageProxy)
                        }
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Camera, contentDescription = "Scan Food", modifier = Modifier.size(40.dp))
            }
        } else {
            // Show a message if permission is denied
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera permission is required.")
            }
        }
    }
}
