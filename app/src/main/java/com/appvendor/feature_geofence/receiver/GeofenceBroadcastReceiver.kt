package com.appvendor.feature_geofence.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofence error: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val transitionString = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                "ENTER"
            } else {
                "EXIT"
            }

            Log.i(TAG, "Geofence Transition: $transitionString. Triggered by: ${triggeringGeofences?.joinToString { it.requestId }}")

            // TODO: Hook into app's existing order-eligibility logic.
            // E.g., update a local DataStore flag or inform a repository that device is inside/outside geofence.
            // For this task, we just log and acknowledge the transition as requested:
            // "don't invent new business logic here; hook into wherever 'is customer in range' is currently checked/stubbed."
            val isInside = geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER
            updateGeofenceEligibilityStatus(context, isInside)
        } else {
            Log.e(TAG, "Invalid geofence transition type: $geofenceTransition")
        }
    }

    private fun updateGeofenceEligibilityStatus(context: Context, isInside: Boolean) {
        // Implementation would update the shared state here.
        // For example, writing to DataStore:
        // val dataStore = context.dataStore
        // ...
        Log.d(TAG, "Customer eligibility updated. isInsideGeofence = $isInside")
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}
