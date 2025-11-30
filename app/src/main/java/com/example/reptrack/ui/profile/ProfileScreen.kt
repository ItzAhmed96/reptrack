package com.example.reptrack.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
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
    var postsCount by remember { mutableStateOf(0) }
    var activeWorkouts by remember { mutableStateOf(0) }
    var memberSince by remember { mutableStateOf("") }
    var followersCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }

    fun loadUserData() {
        scope.launch {
            val res = AppModule.authUseCase.getCurrentUser()
            when (res) {
                is com.example.reptrack.common.Resource.Success -> {
                    user = res.data
                    error = null
                    
                    val uid = user?.id ?: ""
                    
                    // Calculate member since
                    val accountCreatedDate = Date(user?.id?.substring(0, 8)?.toLongOrNull(16)?.times(1000) ?: System.currentTimeMillis())
                    memberSince = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(accountCreatedDate)
                    
                    // Load posts count
                    val socialFeedUseCase = AppModule.socialFeedUseCase
                    val postsRes = socialFeedUseCase.getPostsForUser(uid)
                    if (postsRes is com.example.reptrack.common.Resource.Success) {
                        postsCount = postsRes.data?.size ?: 0
                    }
                    
                    // Load active workouts count
                    val workoutsRes = AppModule.workoutPlanRemoteDataSource.getWorkoutPlansForUser(uid)
                    if (workoutsRes is com.example.reptrack.common.Resource.Success) {
                        activeWorkouts = workoutsRes.data?.size ?: 0
                    }
                    
                    // Load social stats
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
    }

    LaunchedEffect(Unit) {
        loadUserData()
    }

    if (showEditDialog && user != null) {
        EditProfileDialog(
            user = user!!,
            onDismiss = { showEditDialog = false },
            onSave = { name, bio, profilePicUrl ->
                scope.launch {
                    val result = AppModule.authDataSource.updateUser(user!!.id, name, bio, profilePicUrl)
                    if (result is com.example.reptrack.common.Resource.Success) {
                        showEditDialog = false
                        loadUserData() // Reload user data
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                actions = {
                    if (user != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Picture
                        if (user!!.profilePicUrl.isNotBlank()) {
                            AsyncImage(
                                model = user!!.profilePicUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initials = (user!!.name.trim().firstOrNull()?.uppercase() ?: "?")
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = user!!.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = user!!.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text(text = user!!.role.ifBlank { "trainee" }.replaceFirstChar { it.uppercase() }) }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Followers and Following
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = followersCount.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Followers", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = followingCount.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Following", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bio Card
                        if (user!!.bio.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Bio",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = user!!.bio, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Stats Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = postsCount.toString(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text("Posts", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = activeWorkouts.toString(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text("Workouts", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Member Since Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Member since ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = memberSince,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Logout Button
                        Button(
                            onClick = {
                                scope.launch {
                                    AppModule.authUseCase.logout()
                                    onLoggedOut()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Log Out")
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
