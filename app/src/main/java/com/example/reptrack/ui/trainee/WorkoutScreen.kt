package com.example.reptrack.ui.trainee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reptrack.di.AppModule
import com.example.reptrack.domain.models.WorkoutPlan
import com.example.reptrack.domain.models.WorkoutDay
import com.example.reptrack.domain.models.Exercise
import kotlinx.coroutines.launch

@Composable
fun WorkoutScreen(
    exerciseId: String,
    onLogSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: WorkoutViewModel = viewModel(
        factory = WorkoutViewModelFactory(
            AppModule.provideTrackProgressUseCase(context.applicationContext),
            exerciseId
        )
    )
    
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Log Workout", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = uiState.weight,
            onValueChange = viewModel::onWeightChange,
            label = { Text("Weight (kg/lbs)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = uiState.reps,
            onValueChange = viewModel::onRepsChange,
            label = { Text("Reps Done") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = uiState.notes,
            onValueChange = viewModel::onNotesChange,
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = viewModel::logProgress,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLogging
        ) {
            if (uiState.isLogging) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Log Set")
            }
        }
        
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        LaunchedEffect(uiState.isSuccess) {
            if (uiState.isSuccess) {
                onLogSuccess()
                viewModel.resetState()
            }
        }
    }
}

// Workout Plan UI Model
data class WorkoutPlanUi(
    val id: String,
    val title: String,
    val description: String,
    val daysPerWeek: Int,
    val focusAreas: String,
    val isJoined: Boolean = false
)

// Workout Home Screen (tab landing)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHomeScreen(
    onExploreClick: () -> Unit,
    onOpenHistory: () -> Unit,
    onCreateWorkout: () -> Unit,
    onWorkoutSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var myWorkouts by remember { mutableStateOf(listOf<WorkoutPlan>()) }
    var isLoading by remember { mutableStateOf(false) }
    var isTrainer by remember { mutableStateOf(false) }
    val userId = AppModule.auth.currentUser?.uid ?: ""

    // Load workouts and user role when screen appears
    LaunchedEffect(Unit) {
        isLoading = true
        
        // Check user role
        val userResult = AppModule.userRepository.getCurrentUser()
        if (userResult is com.example.reptrack.common.Resource.Success) {
            isTrainer = userResult.data?.role == "trainer"
        }
        
        // Load workouts
        val result = AppModule.workoutPlanRemoteDataSource.getWorkoutPlansForUser(userId)
        if (result is com.example.reptrack.common.Resource.Success) {
            myWorkouts = result.data ?: emptyList()
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Workouts") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Only show Create New Workout for trainers
            if (isTrainer) {
                Button(
                    onClick = onCreateWorkout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create New Workout")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onExploreClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Explore Workouts")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Workout History")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (myWorkouts.isEmpty()) {
                Text(
                    text = if (isTrainer) {
                        "You have not created or joined any workouts yet.\nCreate your own or explore plans from other trainers."
                    } else {
                        "You have not joined any workouts yet.\nExplore programs from trainers to get started."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "My Workouts",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(myWorkouts) { workout ->
                        WorkoutPlanCardFromModel(
                            workout = workout,
                            onClick = { onWorkoutSelected(workout.id) },
                            currentUserId = userId,
                            onDelete = { workoutId ->
                                // Delete workout
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    val result = AppModule.workoutPlanRemoteDataSource.deleteWorkoutPlan(workoutId)
                                    if (result is com.example.reptrack.common.Resource.Success) {
                                        // Reload workouts
                                        val updatedResult = AppModule.workoutPlanRemoteDataSource.getWorkoutPlansForUser(userId)
                                        if (updatedResult is com.example.reptrack.common.Resource.Success) {
                                            myWorkouts = updatedResult.data ?: emptyList()
                                        }
                                    }
                                }
                            },
                            onLeave = { workoutId ->
                                // Leave workout
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    val result = AppModule.workoutPlanRemoteDataSource.unjoinWorkoutPlan(workoutId, userId)
                                    if (result is com.example.reptrack.common.Resource.Success) {
                                        // Reload workouts
                                        val updatedResult = AppModule.workoutPlanRemoteDataSource.getWorkoutPlansForUser(userId)
                                        if (updatedResult is com.example.reptrack.common.Resource.Success) {
                                            myWorkouts = updatedResult.data ?: emptyList()
                                        }
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutPlanCard(
    workout: WorkoutPlanUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = workout.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = workout.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${workout.daysPerWeek} days • ${workout.focusAreas}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun WorkoutPlanCardFromModel(
    workout: WorkoutPlan,
    onClick: () -> Unit,
    currentUserId: String = "",
    onDelete: ((String) -> Unit)? = null,
    onLeave: ((String) -> Unit)? = null
) {
    val isCreator = workout.userId == currentUserId || workout.creatorId == currentUserId
    val isJoined = workout.joinedUserIds.contains(currentUserId) && !isCreator
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = workout.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${workout.daysPerWeek} days • ${workout.focusAreas}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    if (workout.restDays.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rest: ${workout.restDays}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                
                // Action buttons
                Column(horizontalAlignment = Alignment.End) {
                    if (isCreator && onDelete != null) {
                        // Show delete button for creator
                        IconButton(onClick = { onDelete(workout.id) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete workout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else if (isJoined && onLeave != null) {
                        // Show leave button for joined workouts
                        TextButton(onClick = { onLeave(workout.id) }) {
                            Text("Leave", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// Explore Workouts Screen
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ExploreWorkoutsScreen(
    onBack: () -> Unit,
    onJoinWorkout: (WorkoutPlan) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var workoutPlans by remember { mutableStateOf(listOf<WorkoutPlan>()) }
    var filteredWorkoutPlans by remember { mutableStateOf(listOf<WorkoutPlan>()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    fun loadWorkoutPlans() {
        scope.launch {
            isLoading = true
            errorMessage = null
            
            try {
                val result = AppModule.workoutPlanRemoteDataSource.getAllWorkoutPlans()
                if (result is com.example.reptrack.common.Resource.Success) {
                    workoutPlans = result.data ?: emptyList()
                    // Initially show all workouts
                    filteredWorkoutPlans = workoutPlans
                } else {
                    errorMessage = result.message ?: "Failed to load workout plans"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred while loading workout plans"
            } finally {
                isLoading = false
            }
        }
    }

    // Filter workouts based on search query
    LaunchedEffect(workoutPlans, searchQuery) {
        filteredWorkoutPlans = if (searchQuery.isBlank()) {
            workoutPlans
        } else {
            workoutPlans.filter { workout ->
                workout.name.contains(searchQuery, ignoreCase = true) ||
                workout.description.contains(searchQuery, ignoreCase = true) ||
                workout.focusAreas.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Load workout plans when screen appears
    LaunchedEffect(Unit) {
        loadWorkoutPlans()
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Header with search
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Title
                        Text(
                            text = "Explore Workouts",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { 
                            Text(
                                text = "Search workouts...",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            cursorColor = MaterialTheme.colorScheme.onPrimary,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading workouts...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { loadWorkoutPlans() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    item {
                        // Header text
                        Text(
                            text = "Discover professional workout plans created by certified trainers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (filteredWorkoutPlans.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = if (searchQuery.isBlank()) Icons.Default.FitnessCenter else Icons.Default.SearchOff,
                                        contentDescription = "No workouts",
                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (searchQuery.isBlank()) {
                                            "No workout plans available yet. Check back later!"
                                        } else {
                                            "No workouts found matching your search."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredWorkoutPlans) { workoutPlan ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    // Workout name
                                    Text(
                                        text = workoutPlan.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Description
                                    Text(
                                        text = workoutPlan.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Tags
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Days per week
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "${workoutPlan.daysPerWeek} Days/Week",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        
                                        // Focus areas
                                        if (workoutPlan.focusAreas.isNotBlank()) {
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = workoutPlan.focusAreas,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Rest days
                                    if (workoutPlan.restDays.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Restaurant,
                                                contentDescription = "Rest days",
                                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Rest: ${workoutPlan.restDays}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Join button
                                    Button(
                                        onClick = {
                                            onJoinWorkout(workoutPlan)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "Join Workout",
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Create Workout Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(
    onBack: () -> Unit,
    onWorkoutCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var workoutName by remember { mutableStateOf("") }
    var workoutDescription by remember { mutableStateOf("") }
    var daysPerWeek by remember { mutableStateOf("") }
    var focusAreas by remember { mutableStateOf("") }
    var restDays by remember { mutableStateOf("") }
    var exercises by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    @Stable
    data class ExerciseOverrideUi(
        var sets: String = "",
        var reps: String = "",
        var restTime: String = ""
    )

    @Stable
    data class WorkoutDayUi(
        var name: String = "",
        var search: String = "",
        var selectedExerciseIds: Set<String> = emptySet(),
        var overrides: Map<String, ExerciseOverrideUi> = emptyMap()
    )

    var allExercises by remember { mutableStateOf(listOf<Exercise>()) }
    var isLoadingExercises by remember { mutableStateOf(false) }
    var days by remember { mutableStateOf(listOf<WorkoutDayUi>()) }

    LaunchedEffect(Unit) {
        isLoadingExercises = true
        val res = AppModule.exerciseRemoteDataSource.getAllExercises()
        if (res is com.example.reptrack.common.Resource.Success) {
            allExercises = res.data ?: emptyList()
        }
        isLoadingExercises = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Create your custom workout split (e.g. Push/Pull/Legs)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = workoutName,
                    onValueChange = { workoutName = it },
                    label = { Text("Workout Name") },
                    placeholder = { Text("e.g. Push Day, Pull Day, Leg Day") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = workoutDescription,
                    onValueChange = { workoutDescription = it },
                    label = { Text("Description") },
                    placeholder = { Text("What this workout focuses on") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = daysPerWeek,
                    onValueChange = { daysPerWeek = it },
                    label = { Text("Days per Week") },
                    placeholder = { Text("e.g. 2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = focusAreas,
                    onValueChange = { focusAreas = it },
                    label = { Text("Focus Areas") },
                    placeholder = { Text("e.g. Chest, Shoulders, Triceps") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = restDays,
                    onValueChange = { restDays = it },
                    label = { Text("Rest Days") },
                    placeholder = { Text("e.g. Sunday, Wednesday") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = exercises,
                    onValueChange = { exercises = it },
                    label = { Text("Exercises") },
                    placeholder = { Text("e.g. Bench Press, Shoulder Press, Tricep Dips") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Workout Days Editor
                Text(text = "Workout Days", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    days = days + WorkoutDayUi()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Add Day")
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (days.isEmpty()) {
                    Text(
                        text = "No days yet. Add a day like 'Push Day', 'Pull Day', or 'Leg Day'.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    days.forEachIndexed { index, day ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "Day ${index + 1}", style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = day.name,
                                    onValueChange = { new ->
                                        days = days.toMutableList().also { list ->
                                            list[index] = list[index].copy(name = new)
                                        }
                                    },
                                    label = { Text("Day Name") },
                                    placeholder = { Text("e.g. Push Day") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = day.search,
                                    onValueChange = { q ->
                                        days = days.toMutableList().also { list ->
                                            list[index] = list[index].copy(search = q)
                                        }
                                    },
                                    label = { Text("Search Exercises") },
                                    placeholder = { Text("Type to search...") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                val filtered = allExercises.filter {
                                    day.search.isBlank() || it.name.contains(day.search, ignoreCase = true)
                                }

                                if (isLoadingExercises) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (filtered.isEmpty()) {
                                        Text("No matching exercises.", style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        filtered.take(20).forEach { ex ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Checkbox(
                                                    checked = day.selectedExerciseIds.contains(ex.id),
                                                    onCheckedChange = { checked ->
                                                        val newSet = day.selectedExerciseIds.toMutableSet()
                                                        val newOverrides = day.overrides.toMutableMap()
                                                        if (checked) {
                                                            newSet.add(ex.id)
                                                            val exDefaults = ExerciseOverrideUi(
                                                                sets = ex.sets.toString(),
                                                                reps = ex.reps.toString(),
                                                                restTime = ex.restTime.toString()
                                                            )
                                                            newOverrides[ex.id] = exDefaults
                                                        } else {
                                                            newSet.remove(ex.id)
                                                            newOverrides.remove(ex.id)
                                                        }
                                                        days = days.toMutableList().also { list ->
                                                            list[index] = list[index].copy(
                                                                selectedExerciseIds = newSet,
                                                                overrides = newOverrides
                                                            )
                                                        }
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = run {
                                                    val n = ex.name.lowercase()
                                                    when {
                                                        n.contains("bench") -> "🏋️"
                                                        n.contains("squat") -> "🦵"
                                                        n.contains("deadlift") -> "🧱"
                                                        n.contains("row") -> "🏋️"
                                                        n.contains("curl") -> "💪"
                                                        n.contains("press") -> "🏋️"
                                                        n.contains("pull") -> "🧗"
                                                        else -> "🏋️"
                                                    }
                                                }, style = MaterialTheme.typography.titleLarge)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = ex.name, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }

                                        // Overrides editor for selected exercises
                                        if (day.selectedExerciseIds.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(text = "Selected Exercises", style = MaterialTheme.typography.titleSmall)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            day.selectedExerciseIds.forEach { exId ->
                                                val ex = allExercises.find { it.id == exId }
                                                if (ex != null) {
                                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                                        Text(text = ex.name, style = MaterialTheme.typography.labelMedium)
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        val override = day.overrides[exId]
                                                        Row(modifier = Modifier.fillMaxWidth()) {
                                                            OutlinedTextField(
                                                                value = override?.sets ?: ex.sets.toString(),
                                                                onValueChange = { v ->
                                                                    val newOverrides = day.overrides.toMutableMap()
                                                                    val cur = newOverrides[exId] ?: ExerciseOverrideUi()
                                                                    newOverrides[exId] = cur.copy(sets = v)
                                                                    days = days.toMutableList().also { list ->
                                                                        list[index] = list[index].copy(overrides = newOverrides)
                                                                    }
                                                                },
                                                                label = { Text("Sets") },
                                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            OutlinedTextField(
                                                                value = override?.reps ?: ex.reps.toString(),
                                                                onValueChange = { v ->
                                                                    val newOverrides = day.overrides.toMutableMap()
                                                                    val cur = newOverrides[exId] ?: ExerciseOverrideUi()
                                                                    newOverrides[exId] = cur.copy(reps = v)
                                                                    days = days.toMutableList().also { list ->
                                                                        list[index] = list[index].copy(overrides = newOverrides)
                                                                    }
                                                                },
                                                                label = { Text("Reps") },
                                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            OutlinedTextField(
                                                                value = override?.restTime ?: ex.restTime.toString(),
                                                                onValueChange = { v ->
                                                                    val newOverrides = day.overrides.toMutableMap()
                                                                    val cur = newOverrides[exId] ?: ExerciseOverrideUi()
                                                                    newOverrides[exId] = cur.copy(restTime = v)
                                                                    days = days.toMutableList().also { list ->
                                                                        list[index] = list[index].copy(overrides = newOverrides)
                                                                    }
                                                                },
                                                                label = { Text("Rest (s)") },
                                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            isCreating = true
                            errorMessage = ""
                            val userId = AppModule.auth.currentUser?.uid ?: ""
                            val workout = WorkoutPlan(
                                userId = userId,
                                name = workoutName,
                                description = workoutDescription,
                                daysPerWeek = daysPerWeek.toIntOrNull() ?: 0,
                                focusAreas = focusAreas,
                                restDays = restDays,
                                exercises = exercises,
                                days = days.map { dayUi ->
                                    WorkoutDay(
                                        name = dayUi.name,
                                        exerciseIds = dayUi.selectedExerciseIds.toList(),
                                        overrides = dayUi.overrides.mapValues { entry ->
                                            com.example.reptrack.domain.models.WorkoutExerciseOverride(
                                                sets = entry.value.sets.toIntOrNull(),
                                                reps = entry.value.reps.toIntOrNull(),
                                                restTime = entry.value.restTime.toIntOrNull()
                                            )
                                        }
                                    )
                                }
                            )
                            val result = AppModule.workoutPlanRemoteDataSource.createWorkoutPlan(workout)
                            isCreating = false
                            when (result) {
                                is com.example.reptrack.common.Resource.Success -> {
                                    onWorkoutCreated()
                                }
                                is com.example.reptrack.common.Resource.Error -> {
                                    errorMessage = result.message ?: "Failed to create workout"
                                }
                                else -> {}
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = workoutName.isNotBlank() && workoutDescription.isNotBlank() && !isCreating
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Create Workout")
                    }
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 Tips:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Create splits like Push/Pull/Legs or Upper/Lower\n" +
                                    "• Specify which muscle groups each day targets\n" +
                                    "• Add rest days between training sessions\n" +
                                    "• List the main exercises for this workout",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// Workout Detail Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var workout by remember { mutableStateOf<WorkoutPlan?>(null) }
    var exercises by remember { mutableStateOf(mapOf<String, Exercise>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(workoutId) {
        isLoading = true
        errorMessage = null
        
        try {
            // Load workout plan
            val workoutResult = AppModule.workoutPlanRemoteDataSource.getWorkoutPlanById(workoutId)
            if (workoutResult is com.example.reptrack.common.Resource.Success) {
                workout = workoutResult.data
                
                // Load all exercises for this workout
                val exerciseMap = mutableMapOf<String, Exercise>()
                val allExercisesResult = AppModule.exerciseRemoteDataSource.getAllExercises()
                if (allExercisesResult is com.example.reptrack.common.Resource.Success) {
                    val allExercises = allExercisesResult.data ?: emptyList()
                    // Create a map of exercise ID to Exercise for quick lookup
                    allExercises.forEach { exercise ->
                        exerciseMap[exercise.id] = exercise
                    }
                }
                exercises = exerciseMap
            } else {
                errorMessage = workoutResult.message ?: "Failed to load workout"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "An error occurred"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(workout?.name ?: "Workout Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else if (workout == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Workout not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                item {
                    // Workout header
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = workout!!.name,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = workout!!.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${workout!!.daysPerWeek} Days/Week",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = workout!!.focusAreas.ifEmpty { "No focus areas specified" },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Rest Days",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = workout!!.restDays.ifEmpty { "None specified" },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Workout days
                    Text(
                        text = "Workout Days",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Display each workout day
                items(workout!!.days) { day ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = day.name,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (day.exerciseIds.isEmpty()) {
                                Text(
                                    text = "No exercises assigned to this day",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                // Display exercises for this day
                                day.exerciseIds.forEachIndexed { index, exerciseId ->
                                    val exercise = exercises[exerciseId]
                                    if (exercise != null) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Exercise icon
                                            Text(
                                                text = run {
                                                    val n = exercise.name.lowercase()
                                                    when {
                                                        n.contains("bench") -> "🏋️"
                                                        n.contains("squat") -> "🦵"
                                                        n.contains("deadlift") -> "🧱"
                                                        n.contains("row") -> "🏋️"
                                                        n.contains("curl") -> "💪"
                                                        n.contains("press") -> "🏋️"
                                                        n.contains("pull") -> "🧗"
                                                        else -> "🏋️"
                                                    }
                                                },
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            // Exercise name and details
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = exercise.name,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                
                                                // Display overrides if they exist
                                                val override = day.overrides[exerciseId]
                                                if (override != null && 
                                                    (override.sets != null || override.reps != null || override.restTime != null)) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = buildString {
                                                            if (override.sets != null) append("${override.sets} sets")
                                                            if (override.reps != null) {
                                                                if (isNotEmpty()) append(", ")
                                                                append("${override.reps} reps")
                                                            }
                                                            if (override.restTime != null) {
                                                                if (isNotEmpty()) append(", ")
                                                                append("${override.restTime}s rest")
                                                            }
                                                        },
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                } else {
                                                    // Display default exercise values
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "${exercise.sets} sets, ${exercise.reps} reps, ${exercise.restTime}s rest",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // Add divider except for last item
                                        if (index < day.exerciseIds.size - 1) {
                                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
