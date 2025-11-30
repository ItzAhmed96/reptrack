package com.example.reptrack.ui.trainer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
fun ManageExercisesScreen(
    programId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ManageExercisesViewModel = viewModel(
        factory = ManageExercisesViewModelFactory(
            AppModule.provideManageProgramUseCase(context.applicationContext),
            programId
        )
    )
    
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openAddDialog) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoadingExercises) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.exercisesError != null) {
                Text(
                    text = uiState.exercisesError!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                if (uiState.exercises.isEmpty()) {
                    Text("No exercises added yet.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.exercises) { exercise ->
                            ExerciseItem(exercise)
                        }
                    }
                }
            }
        }
    }
    
    if (uiState.isAddDialogOpen) {
        AddExerciseDialog(
            uiState = uiState,
            onDismiss = viewModel::closeAddDialog,
            onNameChange = viewModel::onNewExerciseNameChange,
            onSetsChange = viewModel::onNewExerciseSetsChange,
            onRepsChange = viewModel::onNewExerciseRepsChange,
            onRestChange = viewModel::onNewExerciseRestChange,
            onNotesChange = viewModel::onNewExerciseNotesChange,
            onAdd = viewModel::addExercise
        )
    }
}

@Composable
fun ExerciseItem(exercise: Exercise) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sets: ${exercise.sets} | Reps: ${exercise.reps} | Rest: ${exercise.restTime}s",
                style = MaterialTheme.typography.bodyMedium
            )
            if (exercise.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exercise.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddExerciseDialog(
    uiState: ManageExercisesUiState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onSetsChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onRestChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exercise") },
        text = {
            Column {
                OutlinedTextField(value = uiState.newExerciseName, onValueChange = onNameChange, label = { Text("Name") })
                OutlinedTextField(value = uiState.newExerciseSets, onValueChange = onSetsChange, label = { Text("Sets") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = uiState.newExerciseReps, onValueChange = onRepsChange, label = { Text("Reps") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = uiState.newExerciseRest, onValueChange = onRestChange, label = { Text("Rest (sec)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = uiState.newExerciseNotes, onValueChange = onNotesChange, label = { Text("Notes") })
                
                if (uiState.addExerciseError != null) {
                    Text(text = uiState.addExerciseError, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAdd,
                enabled = !uiState.isAddingExercise
            ) {
                if (uiState.isAddingExercise) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
