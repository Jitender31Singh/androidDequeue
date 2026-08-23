package com.appvendor.feature_geofence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.feature_geofence.domain.model.GeofenceArea
import com.appvendor.feature_geofence.domain.usecase.GetGeofenceUseCase
import com.appvendor.feature_geofence.domain.usecase.ToggleGeofenceUseCase
import com.appvendor.feature_geofence.domain.usecase.UpdateGeofenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing the state and events for GeofenceScreen.
 */
@HiltViewModel
class GeofenceViewModel @Inject constructor(
    private val getGeofenceUseCase: GetGeofenceUseCase,
    private val updateGeofenceUseCase: UpdateGeofenceUseCase,
    private val toggleGeofenceUseCase: ToggleGeofenceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GeofenceState())
    val state: StateFlow<GeofenceState> = _state.asStateFlow()

    init {
        loadGeofence()
    }

    private fun loadGeofence() {
        getGeofenceUseCase()
            .onEach { area ->
                _state.value = GeofenceState(
                    area = area ?: GeofenceArea(latitude = 0.0, longitude = 0.0, radiusInMeters = 1000f, isEnabled = false),
                    isLoading = false
                )
            }
            .catch { e ->
                _state.value = GeofenceState(error = e.message ?: "Failed to load geofence", isLoading = false)
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: GeofenceEvent) {
        when (event) {
            is GeofenceEvent.UpdateLatitude -> {
                val current = _state.value.area ?: return
                _state.value = _state.value.copy(area = current.copy(latitude = event.latitude))
            }
            is GeofenceEvent.UpdateLongitude -> {
                val current = _state.value.area ?: return
                _state.value = _state.value.copy(area = current.copy(longitude = event.longitude))
            }
            is GeofenceEvent.UpdateRadius -> {
                val current = _state.value.area ?: return
                _state.value = _state.value.copy(area = current.copy(radiusInMeters = event.radius))
            }
            is GeofenceEvent.ToggleGeofence -> {
                val current = _state.value.area ?: return
                viewModelScope.launch {
                    try {
                        toggleGeofenceUseCase(event.isEnabled)
                        _state.value = _state.value.copy(area = current.copy(isEnabled = event.isEnabled))
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(error = "Failed to toggle geofence")
                    }
                }
            }
            is GeofenceEvent.SaveGeofence -> {
                val current = _state.value.area ?: return
                viewModelScope.launch {
                    try {
                        updateGeofenceUseCase(current)
                        // Trigger a UI event for success if needed
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(error = e.message ?: "Failed to save geofence")
                    }
                }
            }
            is GeofenceEvent.DismissError -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }
}

/**
 * Events triggered from the UI.
 */
sealed class GeofenceEvent {
    data class UpdateLatitude(val latitude: Double) : GeofenceEvent()
    data class UpdateLongitude(val longitude: Double) : GeofenceEvent()
    data class UpdateRadius(val radius: Float) : GeofenceEvent()
    data class ToggleGeofence(val isEnabled: Boolean) : GeofenceEvent()
    object SaveGeofence : GeofenceEvent()
    object DismissError : GeofenceEvent()
}
