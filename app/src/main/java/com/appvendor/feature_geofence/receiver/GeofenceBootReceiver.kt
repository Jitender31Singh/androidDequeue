package com.appvendor.feature_geofence.receiver

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.appvendor.feature_geofence.domain.repository.GeofenceRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GeofenceBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: GeofenceRepository

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("GeofenceBootReceiver", "Device rebooted. Re-registering geofences...")
            
            val geofencingClient = LocationServices.getGeofencingClient(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                val area = repository.getGeofence().firstOrNull()
                if (area != null && area.isEnabled) {
                    val geofenceIntent = Intent(context, GeofenceBroadcastReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        geofenceIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    
                    val geofence = Geofence.Builder()
                        .setRequestId("SHOP_GEOFENCE")
                        .setCircularRegion(area.latitude, area.longitude, area.radiusInMeters)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setNotificationResponsiveness(5 * 60 * 1000)
                        .build()

                    val geofencingRequest = GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .addGeofence(geofence)
                        .build()

                    try {
                        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                        Log.i("GeofenceBootReceiver", "Successfully re-registered geofence on boot.")
                    } catch (e: Exception) {
                        Log.e("GeofenceBootReceiver", "Failed to re-register geofence", e)
                    }
                }
            }
        }
    }
}
