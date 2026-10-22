package com.zekibiyikli.marticase.presentation.map

import com.zekibiyikli.marticase.domain.model.LocationPoint
import com.zekibiyikli.marticase.domain.model.MapStyle
import com.zekibiyikli.marticase.presentation.map.model.MarkerDetails

data class MapUiState(
    val isTracking: Boolean = false,
    val markers: List<LocationPoint> = emptyList(),
    val currentLocation: LocationPoint? = null,
    val selectedMarker: MarkerDetails? = null,
    val mapStyle: MapStyle = MapStyle.NORMAL
)
