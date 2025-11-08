package com.example.nourishfit.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
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
    onFoodScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val cameraController = remember { LifecycleCameraController(context) }
    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    var currentLabel by remember { mutableStateOf("Point at your food...") }
    var lastAnalyzedTimestamp by remember { mutableLongStateOf(0L) }

    // --- NEW: State for the confirmation screen ---
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    val imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
        // Only analyze if we are in "live" mode
        if (!isAnalyzing) {
            imageProxy.close()
            return@Analyzer
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalyzedTimestamp >= 1000) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        currentLabel = labels.firstOrNull()?.text ?: "..."
                        Log.d("ML_SCANNER", "Live label: $currentLabel")
                    }
                    .addOnFailureListener { e -> Log.e("ML_SCANNER", "Failed to analyze image", e) }
                    .addOnCompleteListener { imageProxy.close() }
            }
            lastAnalyzedTimestamp = currentTime
        } else {
            imageProxy.close()
        }
    }

    LaunchedEffect(cameraController) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }

        cameraController.bindToLifecycle(lifecycleOwner)
        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraController.imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            imageAnalyzer
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            // --- NEW: This Crossfade switches between Camera and Confirmation ---
            Crossfade(targetState = capturedBitmap, label = "CameraCrossfade", animationSpec = tween(300)) { bitmap ->
                if (bitmap == null) {
                    // --- STATE 1: Live Camera Preview ---
                    CameraPreview(
                        cameraController = cameraController,
                        currentLabel = currentLabel,
                        isLoading = isLoading,
                        onCaptureClick = {
                            isLoading = true
                            isAnalyzing = false // Stop live analysis
                            cameraController.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        capturedBitmap = image.toBitmap()
                                        image.close()
                                        isLoading = false
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("ML_SCANNER", "Failed to take picture", exception)
                                        isLoading = false
                                        isAnalyzing = true // Re-start analysis on error
                                    }
                                }
                            )
                        }
                    )
                } else {
                    // --- STATE 2: Confirmation Screen ---
                    ConfirmScreen(
                        bitmap = bitmap,
                        initialLabel = currentLabel,
                        onConfirm = { finalLabel ->
                            onFoodScanned(finalLabel)
                        },
                        onRetake = {
                            capturedBitmap = null // Clear bitmap
                            isAnalyzing = true // Re-start live analysis
                        }
                    )
                }
            }
        } else {
            // Permission Denied State
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera permission is required.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

// --- NEW COMPOSABLE: The Live Camera Preview UI ---
@Composable
fun CameraPreview(
    cameraController: LifecycleCameraController,
    currentLabel: String,
    isLoading: Boolean,
    onCaptureClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply { this.controller = cameraController }
            },
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = currentLabel,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = onCaptureClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp), color = Color.White)
            } else {
                Icon(Icons.Default.Camera, contentDescription = "Scan Food", modifier = Modifier.size(40.dp))
            }
        }
    }
}

// --- NEW COMPOSABLE: The Confirmation UI ---
@Composable
fun ConfirmScreen(
    bitmap: Bitmap,
    initialLabel: String,
    onConfirm: (String) -> Unit,
    onRetake: () -> Unit
) {
    var editedLabel by remember { mutableStateOf(initialLabel) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Show the captured image
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Captured Food",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Scrim for better UI visibility
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Editable Text Field for the label
            OutlinedTextField(
                value = editedLabel,
                onValueChange = { editedLabel = it },
                label = { Text("Food Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // "Retake" and "Add" buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onRetake,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = "Retake")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retake")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { onConfirm(editedLabel) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Diet")
                }
            }
        }
    }
}

