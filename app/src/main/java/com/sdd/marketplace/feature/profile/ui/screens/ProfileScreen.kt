package com.sdd.marketplace.feature.profile.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.sdd.marketplace.core.navigation.Screen
import com.sdd.marketplace.core.ui.components.*
import com.sdd.marketplace.core.ui.theme.*
import com.sdd.marketplace.feature.profile.viewmodel.ProfileEvent
import com.sdd.marketplace.feature.profile.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val products = viewModel.userProducts.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.NavigateToChat -> navController.navigate(Screen.ChatDetail.createRoute(event.chatId))
                is ProfileEvent.NavigateToLogin -> navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                else -> {}
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Top bar
        if (!uiState.isCurrentUser) {
            TopAppBar(
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Back") } },
                title = { Text(uiState.user?.fullName ?: "Profile", fontWeight = FontWeight.Bold) }
            )
        } else {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Outlined.Settings, "Settings") }
                }
            )
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Profile Header
            Box(
                Modifier.fillMaxWidth().background(SddPink.copy(alpha = 0.05f)).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        AsyncImage(
                            model = uiState.user?.avatarUrl,
                            contentDescription = uiState.user?.fullName,
                            modifier = Modifier.size(90.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        if (uiState.isCurrentUser) {
                            Box(
                                Modifier.size(28.dp).clip(CircleShape).background(SddPink).align(Alignment.BottomEnd).clickable { },
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Filled.Edit, "Edit", tint = Color.White, modifier = Modifier.size(14.dp)) }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(uiState.user?.fullName ?: "User", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        if (uiState.user?.isVerified == true) { Spacer(Modifier.width(6.dp)); VerifiedBadge() }
                    }
                    uiState.user?.bio?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    uiState.user?.location?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, "Location", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ProfileStat("${uiState.user?.productCount ?: 0}", "Products")
                        ProfileStat("${uiState.user?.followerCount ?: 0}", "Followers")
                        ProfileStat("${uiState.user?.followingCount ?: 0}", "Following")
                        ProfileStat("${uiState.user?.soldCount ?: 0}", "Sold")
                    }
                    Spacer(Modifier.height(12.dp))
                    if (!uiState.isCurrentUser) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.followUnfollow() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.isFollowing) MaterialTheme.colorScheme.surfaceVariant else SddPink
                                ),
                                modifier = Modifier.weight(1f)
                            ) { Text(if (uiState.isFollowing) "Following" else "Follow", color = if (uiState.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White) }
                            OutlinedButton(
                                onClick = { viewModel.messageUser() },
                                border = BorderStroke(1.dp, SddPink),
                                modifier = Modifier.weight(1f)
                            ) { Text("Message", color = SddPink) }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { },
                                border = BorderStroke(1.dp, SddPink), modifier = Modifier.weight(1f)
                            ) { Text("Edit Profile", color = SddPink) }
                            Button(
                                onClick = { viewModel.requestVerification() },
                                colors = ButtonDefaults.buttonColors(containerColor = SddPink),
                                modifier = Modifier.weight(1f)
                            ) { Text("Get Verified") }
                        }
                    }
                }
            }

            // Tab Selection
            val tabs = if (uiState.isCurrentUser) listOf("Listings", "Sold", "Saved", "Reviews") else listOf("Shop", "Reviews")
            TabRow(selectedTabIndex = uiState.selectedTab, containerColor = MaterialTheme.colorScheme.surface, contentColor = SddPink) {
                tabs.forEachIndexed { index, tab ->
                    Tab(selected = uiState.selectedTab == index, onClick = { viewModel.selectTab(index) }) {
                        Text(tab, modifier = Modifier.padding(vertical = 12.dp))
                    }
                }
            }

            // Products Grid
            when (uiState.selectedTab) {
                0 -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(600.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products.itemCount) { index ->
                            products[index]?.let { product ->
                                ProductCard(
                                    imageUrl = product.images.firstOrNull(),
                                    title = product.title, price = product.price,
                                    sellerName = uiState.user?.fullName ?: "",
                                    sellerAvatarUrl = uiState.user?.avatarUrl,
                                    isVerified = uiState.user?.isVerified == true,
                                    isFavorite = false,
                                    onFavoriteClick = { },
                                    onClick = { navController.navigate(Screen.ProductDetail.createRoute(product.id)) }
                                )
                            }
                        }
                    }
                }
            }

            // Sign out for current user
            if (uiState.isCurrentUser) {
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Icon(Icons.Outlined.Logout, "Sign out", tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
