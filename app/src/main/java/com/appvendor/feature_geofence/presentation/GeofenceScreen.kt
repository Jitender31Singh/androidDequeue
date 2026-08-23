package com.appvendor.feature_geofence.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.compose.material.icons.filled.Warning
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvendor.feature_geofence.presentation.components.*
import com.appvendor.feature_geofence.receiver.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

@Composable
fun GeofenceScreen(
    viewModel: GeofenceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val geofencingClient = remember { LocationServices.getGeofencingClient(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // ── Permissions Handling ──
    var hasForegroundLocation by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasBackgroundLocation by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasBackgroundLocation = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Geofencing works best with background location enabled.", Toast.LENGTH_LONG).show()
        }
    }

    val foregroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        hasForegroundLocation = fineGranted
        
        if (fineGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundLocation) {
            // Android 11+ requires 2-step permission prompt. Only request BG after FG is granted.
            backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    LaunchedEffect(hasForegroundLocation) {
        if (!hasForegroundLocation) {
            foregroundPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (!hasBackgroundLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // If they already have foreground but not background, ask for background separately
            backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B0F0D),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Service Area Geofence") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0F0D),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (state.area != null) {
                Surface(
                    color = Color(0xFF0B0F0D),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            viewModel.onEvent(GeofenceEvent.SaveGeofence)
                            state.area?.let { area ->
                                handleGeofenceRegistration(context, geofencingClient, area.isEnabled, area.latitude, area.longitude, area.radiusInMeters)
                            }
                            Toast.makeText(context, "Geofence settings saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp)
                    ) {
                        Text("Save Configuration")
                    }
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                state.error?.let { errorMsg ->
                    ErrorCard(error = errorMsg, onDismiss = { viewModel.onEvent(GeofenceEvent.DismissError) })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                state.area?.let { area ->
                    GeofenceHeader(isEnabled = area.isEnabled)
                    
                    if (!hasBackgroundLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Text(
                            text = "Warning: Background location is denied. Geofence transitions may only trigger when the app is open.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    GeofenceToggleRow(
                        isEnabled = area.isEnabled,
                        onToggle = { isEnabled ->
                            if (isEnabled) {
                                if (!hasForegroundLocation) {
                                    foregroundPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                } else if (!hasBackgroundLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                }
                            }
                            viewModel.onEvent(GeofenceEvent.ToggleGeofence(isEnabled))
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GeofenceMapCard(
                        latitude = area.latitude,
                        longitude = area.longitude,
                        radiusMeters = area.radiusInMeters.toInt(),
                        isEnabled = area.isEnabled,
                        hasLocationPermission = hasForegroundLocation,
                        onMapClick = { lat, lng ->
                            viewModel.onEvent(GeofenceEvent.UpdateLatitude(lat))
                            viewModel.onEvent(GeofenceEvent.UpdateLongitude(lng))
                        },
                        onUseMyLocation = {
                            if (hasForegroundLocation) {
                                @SuppressLint("MissingPermission")
                                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                    .addOnSuccessListener { location: Location? ->
                                        location?.let {
                                            viewModel.onEvent(GeofenceEvent.UpdateLatitude(it.latitude))
                                            viewModel.onEvent(GeofenceEvent.UpdateLongitude(it.longitude))
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
                                foregroundPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )

                    CoordinatesReadout(latitude = area.latitude, longitude = area.longitude)
                    Spacer(modifier = Modifier.height(16.dp))

                    RadiusSelector(
                        radiusMeters = area.radiusInMeters.toInt(),
                        onRadiusChange = { viewModel.onEvent(GeofenceEvent.UpdateRadius(it.toFloat())) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    HowItWorksSection()

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun handleGeofenceRegistration(
    context: Context,
    geofencingClient: GeofencingClient,
    isEnabled: Boolean,
    lat: Double,
    lng: Double,
    radius: Float
) {
    val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )

    if (!isEnabled) {
        geofencingClient.removeGeofences(pendingIntent)
        return
    }

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return // Cannot add without permission
    }

    val geofence = Geofence.Builder()
        .setRequestId("SHOP_GEOFENCE")
        .setCircularRegion(lat, lng, radius)
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
        .setNotificationResponsiveness(5 * 60 * 1000) // 5 minutes responsiveness for battery
        .build()

    val geofencingRequest = GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofence(geofence)
        .build()

    geofencingClient.addGeofences(geofencingRequest, pendingIntent).addOnFailureListener {
        it.printStackTrace()
    }
}

@Composable
fun ErrorCard(error: String, onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}
