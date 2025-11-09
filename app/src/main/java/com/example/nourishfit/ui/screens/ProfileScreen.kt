package com.example.nourishfit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nourishfit.R
import com.example.nourishfit.ui.viewmodel.ProfileData
import com.example.nourishfit.ui.viewmodel.ProfileViewModel
import com.example.nourishfit.ui.viewmodel.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModelFactory: ProfileViewModelFactory,
    onNavigateUp: () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
    val profileState by viewModel.profileState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // This effect will show a snackbar when the save is successful
    LaunchedEffect(profileState.isSaveSuccessful) {
        if (profileState.isSaveSuccessful) {
            snackbarHostState.showSnackbar("Profile Saved!")
            viewModel.resetSaveSuccess()
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                // Profile Picture (using a placeholder for now)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profileState.profile.photoUrl.ifEmpty { "https://placehold.co/200x200/e0e0e0/757575?text=${profileState.profile.name.firstOrNull() ?: 'P'}" })
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
                    value = profileState.profile.name,
                    onValueChange = { viewModel.updateProfile(profileState.profile.copy(name = it)) },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Row for Age & Weight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = profileState.profile.age,
                        onValueChange = { viewModel.updateProfile(profileState.profile.copy(age = it)) },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = profileState.profile.weight,
                        onValueChange = { viewModel.updateProfile(profileState.profile.copy(weight = it)) },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = profileState.profile.height,
                    onValueChange = { viewModel.updateProfile(profileState.profile.copy(height = it)) },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Activity Level
                Text("Activity Level", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                ActivityLevelSelector(
                    selectedLevel = profileState.profile.activityLevel,
                    onLevelSelected = { viewModel.updateProfile(profileState.profile.copy(activityLevel = it)) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Calculated TDEE (Calorie Goal)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Estimated Daily Calorie Goal", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${profileState.tdee}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text("calories/day", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Save Button
                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (profileState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Save Profile")
                    }
                }

                if (profileState.error != null) {
                    Text(
                        text = profileState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
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
                shape = SegmentedButtonDefaults.shape(position = index, count = options.size),
                onClick = { onLevelSelected(level) },
                selected = level == selectedLevel
            ) {
                Text(level.displayName, modifier = Modifier.padding(8.dp))
            }
        }
    }
}