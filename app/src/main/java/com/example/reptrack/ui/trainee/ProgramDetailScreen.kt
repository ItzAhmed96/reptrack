package com.example.reptrack.ui.trainee

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reptrack.di.AppModule
import com.example.reptrack.domain.models.Exercise

@Composable
fun ProgramDetailScreen(
    programId: String,
    onNavigateToWorkout: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ProgramDetailViewModel = viewModel(
        factory = ProgramDetailViewModelFactory(
            AppModule.provideManageProgramUseCase(context.applicationContext),
            AppModule.userRepository,
            programId
        )
    )
    
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(text = uiState.program?.name ?: "", style = MaterialTheme.typography.headlineMedium)
                    Text(text = uiState.program?.description ?: "", style = MaterialTheme.typography.bodyMedium)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Exercises", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn {
                        items(uiState.exercises) { exercise ->
                            ExerciseItem(exercise, onClick = { onNavigateToWorkout(exercise.id) })
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { 
                            viewModel.joinProgram {
                                // Show success message or navigate
                                onNavigateBack() // For now, just go back
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Join Program")
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(exercise: Exercise, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = exercise.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "${exercise.sets} sets x ${exercise.reps} reps", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
