package com.zekibiyikli.marticase.presentation.map.model

import com.zekibiyikli.marticase.domain.model.LocationPoint

data class MarkerDetails(
    val number: Int,
    val point: LocationPoint,
    val address: String? = null,
    val isLoadingAddress: Boolean = true
)
