package com.zekibiyikli.marticase.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zekibiyikli.marticase.domain.model.MapStyle
import com.zekibiyikli.marticase.domain.usecase.GetAddressUseCase
import com.zekibiyikli.marticase.domain.usecase.ObserveMapStyleUseCase
import com.zekibiyikli.marticase.domain.usecase.ObserveMarkersUseCase
import com.zekibiyikli.marticase.domain.usecase.SetMapStyleUseCase
import com.zekibiyikli.marticase.presentation.map.model.MarkerDetails
import com.zekibiyikli.marticase.service.RouteTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val tracker: RouteTracker,
    private val getAddress: GetAddressUseCase,
    observeMarkers: ObserveMarkersUseCase,
    observeMapStyle: ObserveMapStyleUseCase,
    private val setMapStyle: SetMapStyleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var addressJob: Job? = null

    init {
        viewModelScope.launch {
            tracker.isTracking.collect { isTracking -> _uiState.update { it.copy(isTracking = isTracking) } }
        }
        viewModelScope.launch {
            tracker.currentLocation.collect { point -> _uiState.update { it.copy(currentLocation = point) } }
        }
        viewModelScope.launch {
            observeMarkers().collect { markers -> _uiState.update { it.copy(markers = markers) } }
        }
        viewModelScope.launch {
            observeMapStyle().collect { style -> _uiState.update { it.copy(mapStyle = style) } }
        }
    }

    fun toggleMapStyle() {
        val next = if (_uiState.value.mapStyle == MapStyle.NORMAL) MapStyle.HYBRID else MapStyle.NORMAL
        viewModelScope.launch { setMapStyle(next) }
    }

    fun clearRoute() {
        dismissMarkerDetails()
        tracker.clearRoute()
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
}
