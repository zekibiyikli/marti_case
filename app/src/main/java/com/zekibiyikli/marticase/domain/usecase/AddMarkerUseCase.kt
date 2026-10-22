package com.zekibiyikli.marticase.domain.usecase

import com.zekibiyikli.marticase.domain.model.LocationPoint
import com.zekibiyikli.marticase.domain.repository.MarkerRepository

class AddMarkerUseCase(private val repository: MarkerRepository) {
    suspend operator fun invoke(point: LocationPoint) = repository.addMarker(point)
}
