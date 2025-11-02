package com.example.nourishfit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// --- NEW: Imports for better keyboard control ---
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
// ---
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nourishfit.ui.viewmodel.LoginUiState
import com.example.nourishfit.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") } // For sign-up
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoginMode by rememberSaveable { mutableStateOf(true) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    // --- NEW: Added for snackbar and focus control ---
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        } else if (uiState is LoginUiState.Error) {
            snackbarHostState.showSnackbar((uiState as LoginUiState.Error).message)
            viewModel.resetState()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // --- NEW: Animated Title and Subtitle ---
            Crossfade(
                targetState = isLoginMode,
                label = "TitleCrossfade",
                animationSpec = tween(500)
            ) { loginMode ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (loginMode) {
                        Text("Welcome Back!", style = MaterialTheme.typography.headlineLarge)
                        Text(
                            "Log in to access your personalized fitness journey.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                        )
                    } else {
                        Text("Create an Account", style = MaterialTheme.typography.headlineLarge)
                        Text(
                            "Start your journey with NourishFit today.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (isLoginMode) ImeAction.Done else ImeAction.Next),
                keyboardActions = KeyboardActions(onDone = { if (isLoginMode) focusManager.clearFocus() }),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Password Icon")
                },
                trailingIcon = {
                    val image = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (isPasswordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )

            // --- NEW: Animated "Confirm Password" Field ---
            AnimatedVisibility(
                visible = !isLoginMode,
                enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(animationSpec = tween(500)),
                exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(animationSpec = tween(500))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Confirm Password Icon")
                        },
                    )
                }
            }


            Spacer(modifier = Modifier.height(32.dp))

            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        // --- NEW: Added client-side validation ---
                        focusManager.clearFocus()
                        if (isLoginMode) {
                            viewModel.signIn(email, password)
                        } else {
                            if (password == confirmPassword) {
                                viewModel.signUp(email, password)
                            } else {
                                // Show an error if passwords don't match
                                scope.launch {
                                    snackbarHostState.showSnackbar("Passwords do not match.")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    // --- NEW: Animated Button Text ---
                    Crossfade(
                        targetState = isLoginMode,
                        animationSpec = tween(200),
                        label = "ButtonTextCrossfade"
                    ) { loginMode ->
                        Text(if (loginMode) "Login" else "Sign Up")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { isLoginMode = !isLoginMode }) {
                // --- NEW: Animated Toggle Text ---
                Crossfade(
                    targetState = isLoginMode,
                    animationSpec = tween(200),
                    label = "ToggleTextCrossfade"
                ) { loginMode ->
                    Text(if (isLoginMode) "Don't have an account? Sign Up" else "Already have an account? Login")
                }
            }
        }
    }
}
