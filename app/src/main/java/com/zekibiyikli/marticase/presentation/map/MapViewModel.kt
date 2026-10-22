package com.zekibiyikli.marticase.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zekibiyikli.marticase.domain.model.LocationPoint
import com.zekibiyikli.marticase.domain.usecase.GetAddressUseCase
import com.zekibiyikli.marticase.domain.usecase.ObserveLocationUpdatesUseCase
import com.zekibiyikli.marticase.util.distanceMeters
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

private const val MarkerDistanceMeters = 100.0

data class MarkerDetails(
    val number: Int,
    val point: LocationPoint,
    val address: String? = null,
    val isLoadingAddress: Boolean = true
)

data class MapUiState(
    val isTracking: Boolean = false,
    val markers: List<LocationPoint> = emptyList(),
    val currentLocation: LocationPoint? = null,
    val selectedMarker: MarkerDetails? = null
)

class MapViewModel(
    private val observeLocationUpdates: ObserveLocationUpdatesUseCase,
    private val getAddress: GetAddressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null
    private var addressJob: Job? = null

    // Reference for the next marker while the route has none: the first location after tracking starts
    // or the location where the route was cleared. Markers are measured from it, never dropped on it.
    private var anchor: LocationPoint? = null

    fun toggleTracking() {
        if (_uiState.value.isTracking) stopTracking() else startTracking()
    }

    fun clearRoute() {
        dismissMarkerDetails()
        anchor = _uiState.value.currentLocation
        _uiState.update { it.copy(markers = emptyList()) }
    }

    fun onMarkerClick(index: Int) {
        val point = _uiState.value.markers.getOrNull(index) ?: return
        _uiState.update { it.copy(selectedMarker = MarkerDetails(number = index + 1, point = point)) }
        addressJob?.cancel()
        addressJob = viewModelScope.launch {
            val address = getAddress(point)
            _uiState.update { state ->
                state.copy(
                    selectedMarker = state.selectedMarker?.copy(address = address, isLoadingAddress = false)
                )
            }
        }
    }

    fun dismissMarkerDetails() {
        addressJob?.cancel()
        _uiState.update { it.copy(selectedMarker = null) }
    }

    private fun startTracking() {
        anchor = null
        _uiState.update { it.copy(isTracking = true) }
        trackingJob = viewModelScope.launch {
            observeLocationUpdates()
                .catch { e ->
                    Timber.e(e, "Location tracking failed")
                    _uiState.update { it.copy(isTracking = false) }
                }
                .collect(::onLocation)
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        _uiState.update { it.copy(isTracking = false) }
    }

    private fun onLocation(point: LocationPoint) {
        _uiState.update { state ->
            val last = state.markers.lastOrNull() ?: anchor
            if (last == null) anchor = point
            val addMarker = last != null && distanceMeters(last, point) >= MarkerDistanceMeters
            Timber.d("Location %s, distance from last marker: %s m, marker added: %s",
                point, last?.let { distanceMeters(it, point).toInt() }, addMarker)
            state.copy(
                currentLocation = point,
                markers = if (addMarker) state.markers + point else state.markers
            )
        }
    }
}
