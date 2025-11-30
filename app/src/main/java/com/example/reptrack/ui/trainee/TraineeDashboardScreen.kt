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
import com.example.reptrack.domain.models.Program

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraineeDashboardScreen(
    onNavigateToProgramDetails: (String) -> Unit,
    onNavigateToProgress: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: TraineeDashboardViewModel = viewModel(
        factory = TraineeDashboardViewModelFactory(
            AppModule.provideManageProgramUseCase(context.applicationContext)
        )
    )
    
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
             CenterAlignedTopAppBar(title = { Text("Available Programs") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onNavigateToProgress) {
                Text("My Progress")
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
                if (uiState.programs.isEmpty()) {
                    Text("No programs available.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.programs) { program ->
                            ProgramItem(program, onClick = { onNavigateToProgramDetails(program.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgramItem(program: Program, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = program.name, style = MaterialTheme.typography.titleMedium)
            Text(text = program.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
