package com.example.nourishfit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nourishfit.R
import com.example.nourishfit.ui.viewmodel.ProfileData
import com.example.nourishfit.ui.viewmodel.ProfileUiState
import com.example.nourishfit.ui.viewmodel.ProfileViewModel
import com.example.nourishfit.ui.viewmodel.ProfileViewModelFactory // <-- FIX: Added import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModelFactory: ProfileViewModelFactory,
    onNavigateUp: () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
    // This is the sealed interface state
    val profileState by viewModel.profileState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- FIX: We must check if the state is 'Success' before using its properties ---
    val currentState = profileState
    if (currentState is ProfileUiState.Success) {
        LaunchedEffect(currentState.isSaveSuccessful) {
            if (currentState.isSaveSuccessful) {
                snackbarHostState.showSnackbar("Profile Saved!")
                viewModel.resetSaveSuccess()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Your Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { innerPadding ->
        // --- FIX: Use a 'when' statement to handle both Loading and Success states ---
        when (currentState) {
            is ProfileUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProfileUiState.Success -> {
                // We pass the 'Success' state to the content, which now safely contains all our data
                ProfileScreenContent(
                    modifier = Modifier.padding(innerPadding),
                    profileData = currentState.profile,
                    tdee = currentState.tdee,
                    isLoading = currentState.isLoading,
                    error = currentState.error,
                    onProfileChanged = { viewModel.updateProfile(it) },
                    onSave = { viewModel.saveProfile() }
                )
            }
        }
    }
}

// --- This composable contains the actual UI, and safely assumes it has the Success data ---
@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    profileData: ProfileData,
    tdee: Int,
    isLoading: Boolean,
    error: String?,
    onProfileChanged: (ProfileData) -> Unit,
    onSave: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profileData.photoUrl.ifEmpty { "https://placehold.co/200x200/e0e0e0/757575?text=${profileData.name.firstOrNull() ?: 'P'}" })
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { /* TODO: Implement image picker */ }) {
                Text("Change Photo")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // User Details
            OutlinedTextField(
                value = profileData.name,
                onValueChange = { onProfileChanged(profileData.copy(name = it)) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = profileData.age,
                    onValueChange = { onProfileChanged(profileData.copy(age = it)) },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = profileData.weight,
                    onValueChange = { onProfileChanged(profileData.copy(weight = it)) },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = profileData.height,
                onValueChange = { onProfileChanged(profileData.copy(height = it)) },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Activity Level", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ActivityLevelSelector(
                selectedLevel = profileData.activityLevel,
                onLevelSelected = { onProfileChanged(profileData.copy(activityLevel = it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your Estimated Daily Calorie Goal", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "$tdee",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text("calories/day", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save Profile")
                }
            }

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLevelSelector(
    selectedLevel: ProfileData.ActivityLevel,
    onLevelSelected: (ProfileData.ActivityLevel) -> Unit
) {
    val options = ProfileData.ActivityLevel.entries

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, level ->
            SegmentedButton(
                // Replace SegmentedButtonDefaults.shape(...) with this:
                shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    options.lastIndex -> RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    else -> RoundedCornerShape(0.dp)
                },
                onClick = { onLevelSelected(level) },
                selected = level == selectedLevel
            ) {
                Text(level.displayName, modifier = Modifier.padding(8.dp))
            }
        }
    }
}