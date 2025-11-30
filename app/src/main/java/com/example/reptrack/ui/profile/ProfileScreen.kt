package com.example.reptrack.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.reptrack.di.AppModule
import com.example.reptrack.domain.models.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var totalWorkouts by remember { mutableStateOf(0) }
    var totalVolume by remember { mutableStateOf(0.0) }
    var lastWorkout by remember { mutableStateOf<String?>(null) }
    var followersCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val res = AppModule.authUseCase.getCurrentUser()
        when (res) {
            is com.example.reptrack.common.Resource.Success -> {
                user = res.data
                error = null
                
                // Load user stats
                val tp = AppModule.provideTrackProgressUseCase(context.applicationContext)
                val uid = user?.id ?: ""
                val pr = tp.getProgressForUser(uid)
                if (pr is com.example.reptrack.common.Resource.Success) {
                    val logs = pr.data ?: emptyList()
                    totalWorkouts = logs.size
                    totalVolume = logs.sumOf { it.weight * it.repsDone }
                    val last = logs.maxByOrNull { it.date }
                    if (last != null) {
                        lastWorkout = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(last.date))
                    }
                }
                
                // Load social stats
                val socialFeedUseCase = AppModule.socialFeedUseCase
                val followersRes = socialFeedUseCase.getFollowersCount(uid)
                if (followersRes is com.example.reptrack.common.Resource.Success) {
                    followersCount = followersRes.data ?: 0
                }
                
                val followingRes = socialFeedUseCase.getFollowingCount(uid)
                if (followingRes is com.example.reptrack.common.Resource.Success) {
                    followingCount = followingRes.data ?: 0
                }
            }
            is com.example.reptrack.common.Resource.Error -> {
                error = res.message
            }
            else -> {}
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                user != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val initials = (user!!.name.trim().firstOrNull()?.uppercase() ?: "?")
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = user!!.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = user!!.email, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text(text = user!!.role.ifBlank { "trainee" }.replaceFirstChar { it.uppercase() }) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bio section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Bio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = user!!.bio.ifEmpty { "No bio yet. Tell others about yourself!" },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Followers and Following row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = followersCount.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Followers",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = followingCount.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Following",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Achievements section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Achievements",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Simple achievement display
                                if (totalWorkouts >= 10) {
                                    Text(
                                        text = "🏋️ 10+ Workouts Completed",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (totalVolume >= 10000) {
                                    Text(
                                        text = "💪 10,000+ Volume Lifted",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (followersCount >= 5) {
                                    Text(
                                        text = "🌟 5+ Followers",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (totalWorkouts == 0) {
                                    Text(
                                        text = "👋 Welcome! Complete your first workout",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = totalWorkouts.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Workouts", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "${totalVolume.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Volume", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = lastWorkout ?: "—", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Last", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    AppModule.authUseCase.logout()
                                    onLoggedOut()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Log out")
                        }
                    }
                }
                else -> {
                    Text(text = "No user information.", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
