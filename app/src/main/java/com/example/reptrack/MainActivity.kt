package com.example.reptrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.example.reptrack.ui.profile.ProfileScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.reptrack.di.AppModule
import com.example.reptrack.ui.Screen
import com.example.reptrack.ui.login.LoginScreen
import com.example.reptrack.ui.register.RegisterScreen
import com.example.reptrack.ui.theme.RepTrackTheme
import com.example.reptrack.ui.trainee.ProgramDetailScreen
import com.example.reptrack.ui.trainee.ProgressHistoryScreen
import com.example.reptrack.ui.trainee.TraineeDashboardScreen
import com.example.reptrack.ui.trainee.WorkoutScreen
import com.example.reptrack.ui.trainee.WorkoutHomeScreen
import com.example.reptrack.ui.trainee.ExploreWorkoutsScreen
import com.example.reptrack.ui.trainee.CreateWorkoutScreen
import com.example.reptrack.ui.trainee.WorkoutDetailScreen
import com.example.reptrack.ui.trainee.ProgramsScreen
import com.example.reptrack.ui.trainer.ProgramCreationScreen
import com.example.reptrack.ui.trainer.ManageExercisesScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.reptrack.ui.trainer.TrainerDashboardScreen
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RepTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RepTrackApp()
                }
            }
        }
    }
}

@Composable
fun RepTrackApp() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route ?: Screen.Login.route
    val showBottomBar = currentRoute != Screen.Login.route && currentRoute != Screen.Register.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.WorkoutTab.route || currentRoute == Screen.ProgressHistory.route,
                        onClick = { navController.navigate(Screen.WorkoutTab.route) },
                        icon = { Icon(Icons.Filled.List, contentDescription = null) },
                        label = { Text("Workout") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Profile.route,
                        onClick = { navController.navigate(Screen.Profile.route) },
                        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        label = { Text("Profile") }
                    )
                }
            }
        }
    ) { _ ->
        NavHost(navController = navController, startDestination = Screen.Login.route) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            // Home & Tabs
            composable(Screen.Home.route) {
                com.example.reptrack.ui.social.SocialFeedScreen(
                    onNavigateToSearch = {
                        navController.navigate(Screen.SearchUser.route)
                    },
                    onNavigateToPostDetail = { postId ->
                        navController.navigate(Screen.PostDetail.createRoute(postId))
                    }
                )
            }
            
            // Post Detail Screen (for viewing comments)
            composable(
                route = Screen.PostDetail.route,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                com.example.reptrack.ui.social.PostDetailScreen(
                    postId = postId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.WorkoutTab.route) {
                WorkoutHomeScreen(
                    onExploreClick = { navController.navigate(Screen.ExploreWorkouts.route) },
                    onOpenHistory = { navController.navigate(Screen.ProgressHistory.route) },
                    onCreateWorkout = { navController.navigate(Screen.CreateWorkout.route) },
                    onWorkoutSelected = { workoutId ->
                        navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
                    }
                )
            }
            composable(Screen.ExploreWorkouts.route) {
                val context = LocalContext.current
                ExploreWorkoutsScreen(
                    onBack = { navController.popBackStack() },
                    onJoinWorkout = { workout ->
                        // Actually join the workout
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val userId = AppModule.auth.currentUser?.uid ?: return@launch
                                val result = AppModule.workoutPlanRemoteDataSource.joinWorkoutPlan(workout.id, userId)
                                
                                withContext(Dispatchers.Main) {
                                    if (result is com.example.reptrack.common.Resource.Success) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Successfully joined ${workout.name}!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        navController.popBackStack()
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Failed to join workout",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                )
            }
            composable(Screen.CreateWorkout.route) {
                CreateWorkoutScreen(
                    onBack = { navController.popBackStack() },
                    onWorkoutCreated = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.WorkoutDetail.route,
                arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getString("workoutId") ?: return@composable
                WorkoutDetailScreen(
                    workoutId = workoutId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Trainer Routes
            composable(Screen.TrainerDashboard.route) {
                ProgramsScreen(
                    onNavigateToProgramDetail = { programId ->
                        navController.navigate(Screen.ProgramDetail.createRoute(programId))
                    },
                    onNavigateToCreateProgram = { navController.navigate(Screen.CreateProgram.route) },
                    isTrainer = true
                )
            }

            composable(Screen.CreateProgram.route) {
                ProgramCreationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ManageExercises.route,
                arguments = listOf(navArgument("programId") { type = NavType.StringType })
            ) { backStackEntry ->
                val programId = backStackEntry.arguments?.getString("programId") ?: return@composable
                ManageExercisesScreen(
                    programId = programId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Trainee Routes
            composable(Screen.TraineeDashboard.route) {
                ProgramsScreen(
                    onNavigateToProgramDetail = { programId ->
                        navController.navigate(Screen.ProgramDetail.createRoute(programId))
                    },
                    onNavigateToCreateProgram = { /* Not used */ },
                    isTrainer = false
                )
            }

            composable(
                route = Screen.ProgramDetail.route,
                arguments = listOf(navArgument("programId") { type = NavType.StringType })
            ) { backStackEntry ->
                val programId = backStackEntry.arguments?.getString("programId") ?: return@composable
                ProgramDetailScreen(
                    programId = programId,
                    onNavigateToWorkout = { exerciseId ->
                        navController.navigate(Screen.Workout.createRoute(exerciseId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Workout.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable
                WorkoutScreen(
                    exerciseId = exerciseId,
                    onLogSuccess = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Social Routes
            composable(Screen.SearchUser.route) {
                com.example.reptrack.ui.social.SearchUserScreen(
                    navController = navController
                )
            }
            
            composable(
                route = Screen.UserProfile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                com.example.reptrack.ui.social.UserProfileScreen(
                    userId = userId,
                    navController = navController
                )
            }

            composable(
                route = Screen.PostDetail.route,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                com.example.reptrack.ui.social.PostDetailScreen(
                    postId = postId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ProgressHistory.route) {
                ProgressHistoryScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
