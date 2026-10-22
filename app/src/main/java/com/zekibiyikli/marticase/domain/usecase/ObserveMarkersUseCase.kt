package com.zekibiyikli.marticase.domain.usecase

import com.zekibiyikli.marticase.domain.model.LocationPoint
import com.zekibiyikli.marticase.domain.repository.MarkerRepository
import kotlinx.coroutines.flow.Flow

class ObserveMarkersUseCase(private val repository: MarkerRepository) {
    operator fun invoke(): Flow<List<LocationPoint>> = repository.observeMarkers()
}
