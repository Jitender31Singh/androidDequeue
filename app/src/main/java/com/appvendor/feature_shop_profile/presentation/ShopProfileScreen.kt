package com.appvendor.feature_shop_profile.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.appvendor.feature_shop_profile.domain.model.SocialLinks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopProfileScreen(
    viewModel: ShopProfileViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showEditProfile by remember { mutableStateOf(false) }
    
    // Maintain state internally when editing
    var editName by remember(state.profile) { mutableStateOf(state.profile?.shopName ?: "") }
    var editDesc by remember(state.profile) { mutableStateOf(state.profile?.description ?: "") }
    var editIg by remember(state.profile) { mutableStateOf(state.profile?.socialLinks?.instagram ?: "") }
    var editFb by remember(state.profile) { mutableStateOf(state.profile?.socialLinks?.facebook ?: "") }
    var editWeb by remember(state.profile) { mutableStateOf(state.profile?.socialLinks?.website ?: "") }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(it, isLogo = true) }
    }
    
    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(it, isLogo = false) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Shop Profile", fontWeight = FontWeight.Bold)
                        Text("Manage your vendor information", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        },
        snackbarHost = {
            state.error?.let { err ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = { TextButton(onClick = { viewModel.dismissError() }) { Text("Dismiss") } }
                ) { Text(err) }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (state.isLoading && state.vendorDetails == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Shop Status Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Operational Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("OPEN", "PAUSED", "CLOSED").forEach { status ->
                                val isSelected = state.vendorDetails?.shopStatus == status
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { if (!isSelected) viewModel.updateStatus(status) },
                                    label = { Text(status, modifier = Modifier.padding(vertical = 4.dp)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Images Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Branding & Images", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(16.dp))
                        
                        Text("Banner Image", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { 
                                    bannerPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (state.profile?.bannerUrl != null) {
                                AsyncImage(
                                    model = state.profile?.bannerUrl,
                                    contentDescription = "Banner",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Tap to upload Banner", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Text("Shop Logo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        
                        Surface(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .clickable { 
                                    logoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ) {
                            if (state.profile?.logoUrl != null) {
                                AsyncImage(
                                    model = state.profile?.logoUrl,
                                    contentDescription = "Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Add Logo", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // Public Profile Text Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Public Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { showEditProfile = !showEditProfile }) { 
                                Text(if (showEditProfile) "Cancel" else "Edit") 
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        if (showEditProfile) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = editName, 
                                    onValueChange = { editName = it }, 
                                    label = { Text("Shop Display Name") }, 
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = editDesc, 
                                    onValueChange = { editDesc = it }, 
                                    label = { Text("Shop Description") }, 
                                    modifier = Modifier.fillMaxWidth(), 
                                    maxLines = 3,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = editIg, 
                                    onValueChange = { editIg = it }, 
                                    label = { Text("Instagram Username/URL") }, 
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = editFb, 
                                    onValueChange = { editFb = it }, 
                                    label = { Text("Facebook Username/URL") }, 
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = editWeb, 
                                    onValueChange = { editWeb = it }, 
                                    label = { Text("Website URL") }, 
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { 
                                        viewModel.updateProfile(editName, editDesc, SocialLinks(editIg.ifEmpty { null }, editFb.ifEmpty { null }, editWeb.ifEmpty { null }))
                                        showEditProfile = false
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) { 
                                    if (state.isUpdating) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    } else {
                                        Text("Save Profile") 
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text("Display Name", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(state.profile?.shopName ?: "Not set", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                }
                                
                                Column {
                                    Text("Description", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(state.profile?.description ?: "No description provided.", style = MaterialTheme.typography.bodyLarge)
                                }
                                
                                if (state.profile?.socialLinks != null) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Social Links", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        
                                        if (state.profile?.socialLinks?.instagram != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(state.profile?.socialLinks?.instagram ?: "")
                                            }
                                        }
                                        if (state.profile?.socialLinks?.facebook != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(state.profile?.socialLinks?.facebook ?: "")
                                            }
                                        }
                                        if (state.profile?.socialLinks?.website != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(state.profile?.socialLinks?.website ?: "")
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
    }
}
