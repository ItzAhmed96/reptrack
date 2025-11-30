package com.example.reptrack.ui.trainee

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
import com.example.reptrack.domain.models.ProgressLog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressHistoryScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ProgressHistoryViewModel = viewModel(
        factory = ProgressHistoryViewModelFactory(
            AppModule.provideTrackProgressUseCase(context.applicationContext)
        )
    )
    
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Workout History") })
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
                if (uiState.history.isEmpty()) {
                    Text("No history yet.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.history) { log ->
                            LogItem(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItem(log: ProgressLog) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = dateFormat.format(Date(log.date)), style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${log.weight} kg x ${log.repsDone} reps", style = MaterialTheme.typography.titleMedium)
            if (log.notes.isNotEmpty()) {
                Text(text = "Notes: ${log.notes}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
