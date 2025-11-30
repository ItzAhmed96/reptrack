package com.example.reptrack.ui.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.reptrack.domain.models.Post
import com.example.reptrack.domain.models.User
import com.example.reptrack.ui.social.components.PostCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    navController: NavController,
    viewModel: SocialFeedViewModel = viewModel()
) {
    // We'll need to fetch user details and their posts
    // For now, we'll reuse SocialFeedViewModel but ideally this should have its own ViewModel
    // or extend SocialFeedViewModel to handle specific user profile fetching
    
    val user by viewModel.userProfile.collectAsState()
    val isFollowing by viewModel.isFollowingProfile.collectAsState()
    val followersCount by viewModel.profileFollowersCount.collectAsState()
    val followingCount by viewModel.profileFollowingCount.collectAsState()
    val userPosts by viewModel.profilePosts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val currentUserId = viewModel.getCurrentUserId()

    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.name ?: "Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = user?.profilePicUrl?.ifEmpty { "https://via.placeholder.com/100" },
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = user?.name ?: "User Name",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (!user?.bio.isNullOrBlank()) {
                            Text(
                                text = user?.bio ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(count = userPosts.size, label = "Posts")
                            StatItem(count = followersCount, label = "Followers")
                            StatItem(count = followingCount, label = "Following")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Follow Button
                        if (currentUserId != userId) {
                            Button(
                                onClick = {
                                    if (isFollowing) {
                                        viewModel.unfollowUser(userId)
                                    } else {
                                        viewModel.followUser(userId)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isFollowing) "Unfollow" else "Follow")
                            }
                        }
                    }
                }
                
                // User Posts
                items(userPosts) { post ->
                    PostCard(
                        post = post,
                        isLiked = viewModel.isPostLiked(post.id),
                        currentUserId = currentUserId,
                        onLikeClick = { viewModel.toggleLike(post.id) },
                        onCommentClick = { /* Navigate to post detail */ },
                        onDeleteClick = { viewModel.deletePost(post.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
