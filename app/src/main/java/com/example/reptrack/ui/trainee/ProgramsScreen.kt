package com.example.reptrack.ui.trainee

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.reptrack.domain.models.Program

@Composable
fun ProgramsScreen(
    onNavigateToProgramDetail: (String) -> Unit,
    onNavigateToCreateProgram: () -> Unit,
    isTrainer: Boolean = false // Pass this from parent or check user role here
) {
    val context = LocalContext.current
    val viewModel: ProgramsViewModel = viewModel(
        factory = ProgramsViewModelFactory(
            AppModule.provideManageProgramUseCase(context.applicationContext)
        )
    )
    
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            if (isTrainer) {
                FloatingActionButton(onClick = onNavigateToCreateProgram) {
                    Icon(Icons.Default.Add, contentDescription = "Create Program")
                }
            }
        }
    ) { paddingValues ->
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
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    item {
                        Text("Training Programs", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    items(uiState.programs) { program ->
                        ProgramCard(program, onClick = { onNavigateToProgramDetail(program.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun ProgramCard(program: Program, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = program.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = program.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Created by: ${program.trainerName.ifEmpty { "Unknown" }}", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onClick, modifier = Modifier.align(Alignment.End)) {
                Text("View Program")
            }
        }
    }
}
