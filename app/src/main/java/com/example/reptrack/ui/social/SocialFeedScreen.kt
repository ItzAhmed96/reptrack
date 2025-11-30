package com.example.reptrack.ui.social

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reptrack.ui.social.components.CreatePostDialog
import com.example.reptrack.ui.social.components.PostCard
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialFeedScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    viewModel: SocialFeedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val currentUserId = viewModel.getCurrentUserId()

    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }

    // Load appropriate posts when tab changes
    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            viewModel.loadPosts(isRefresh = true)
        } else {
            viewModel.loadFollowingPosts(isRefresh = true)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            com.example.reptrack.ui.components.RepTrackLogo(size = 32.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RepTrack",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCreatePostDialog = true }) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(Icons.Filled.Search, contentDescription = "Search Users")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("For You") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Following") }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = uiState.isRefreshing),
                onRefresh = { 
                    if (selectedTab == 0) {
                        viewModel.loadPosts(isRefresh = true)
                    } else {
                        viewModel.loadFollowingPosts(isRefresh = true)
                    }
                }
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.posts) { post ->
                        PostCard(
                            post = post,
                            isLiked = viewModel.isPostLiked(post.id),
                            currentUserId = currentUserId,
                            onLikeClick = { viewModel.toggleLike(post.id) },
                            onCommentClick = { onNavigateToPostDetail(post.id) },
                            onDeleteClick = { viewModel.deletePost(post.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }

                    if (uiState.isLoading && uiState.posts.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreatePostDialog) {
        CreatePostDialog(
            onDismiss = { showCreatePostDialog = false },
            onPostCreated = { content, imageUrl ->
                viewModel.createPost(content, imageUrl)
                showCreatePostDialog = false
            }
        )
    }
}