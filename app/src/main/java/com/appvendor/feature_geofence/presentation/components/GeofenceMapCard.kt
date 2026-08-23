package com.appvendor.feature_geofence.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@SuppressLint("MissingPermission") // Permissions handled at screen level
@Composable
fun GeofenceMapCard(
    latitude: Double,
    longitude: Double,
    radiusMeters: Int,
    isEnabled: Boolean,
    hasLocationPermission: Boolean,
    onMapClick: (lat: Double, lng: Double) -> Unit,
    onUseMyLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = if (isEnabled) MaterialTheme.colorScheme.primary else Color.Gray

    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp) // Fixed height since it's in a scrollable column
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141917))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    val cartoLight = object : org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase(
                        "CartoVoyager", 
                        1, 20, 256, ".png", 
                        arrayOf(
                            "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                            "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                            "https://c.basemaps.cartocdn.com/rastertiles/voyager/",
                            "https://d.basemaps.cartocdn.com/rastertiles/voyager/"
                        )
                    ) {
                        override fun getTileURLString(pMapTileIndex: Long): String {
                            return baseUrl + org.osmdroid.util.MapTileIndex.getZoom(pMapTileIndex) + "/" + org.osmdroid.util.MapTileIndex.getX(pMapTileIndex) + "/" + org.osmdroid.util.MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
                        }
                    }
                    setTileSource(cartoLight)

                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(latitude, longitude))

                    // Map Events
                    val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let { onMapClick(it.latitude, it.longitude) }
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    })
                    overlays.add(mapEventsOverlay)
                }
            },
            update = { mapView ->
                val center = GeoPoint(latitude, longitude)
                
                // Animate to new center
                mapView.controller.animateTo(center)
                
                val zoomLevel = when {
                    radiusMeters <= 200 -> 16.0
                    radiusMeters <= 500 -> 15.0
                    radiusMeters <= 1000 -> 14.0
                    else -> 13.0
                }
                mapView.controller.setZoom(zoomLevel)

                // Clear previous overlays except events overlay
                mapView.overlays.removeAll { it !is MapEventsOverlay }

                // Add Circle (Polygon representation of circle in OSMDroid)
                val circle = Polygon(mapView).apply {
                    points = Polygon.pointsAsCircle(center, radiusMeters.toDouble())
                    fillColor = accentColor.copy(alpha = 0.2f).toArgb()
                    strokeColor = accentColor.toArgb()
                    strokeWidth = 4f
                }
                mapView.overlays.add(circle)

                // Add Marker
                val marker = Marker(mapView).apply {
                    position = center
                    title = "Shop Location"
                    snippet = "Tap map to reposition"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(marker)
                
                mapView.invalidate()
            }
        )

        // Custom My Location Button
        FloatingActionButton(
            onClick = onUseMyLocation,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(48.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Use My Current Location",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
