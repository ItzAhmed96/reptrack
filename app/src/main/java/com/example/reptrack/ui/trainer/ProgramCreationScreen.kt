package com.example.reptrack.ui.trainer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reptrack.di.AppModule
import com.example.reptrack.domain.models.Exercise

@Composable
fun ProgramCreationScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ProgramCreationViewModel = viewModel(
        factory = ProgramCreationViewModelFactory(
            AppModule.provideManageProgramUseCase(context.applicationContext),
            AppModule.userRepository
        )
    )
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Local state for new exercise input
    var newExerciseName by remember { mutableStateOf("") }
    var newExerciseSets by remember { mutableStateOf("") }
    var newExerciseReps by remember { mutableStateOf("") }

    if (uiState.isSaved) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    item {
                        Text("Create New Program", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = { Text("Program Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = { viewModel.updateDescription(it) },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Exercises", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Add Exercise Form
                        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                OutlinedTextField(
                                    value = newExerciseName,
                                    onValueChange = { newExerciseName = it },
                                    label = { Text("Exercise Name") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = newExerciseSets,
                                        onValueChange = { newExerciseSets = it },
                                        label = { Text("Sets") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = newExerciseReps,
                                        onValueChange = { newExerciseReps = it },
                                        label = { Text("Reps") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (newExerciseName.isNotBlank() && newExerciseSets.isNotBlank() && newExerciseReps.isNotBlank()) {
                                            viewModel.addExercise(
                                                newExerciseName,
                                                newExerciseSets.toIntOrNull() ?: 0,
                                                newExerciseReps.toIntOrNull() ?: 0
                                            )
                                            newExerciseName = ""
                                            newExerciseSets = ""
                                            newExerciseReps = ""
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Add Exercise")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    items(uiState.exercises) { exercise ->
                        ExerciseItem(exercise, onDelete = { viewModel.removeExercise(exercise) })
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                viewModel.saveProgram()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.name.isNotBlank() && uiState.exercises.isNotEmpty()
                        ) {
                            Text("Save Program")
                        }
                    }
                }
            }
            
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ExerciseItem(exercise: Exercise, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = exercise.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "${exercise.sets} sets x ${exercise.reps} reps", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove Exercise")
            }
        }
    }
}
