package com.zekibiyikli.marticase.domain.usecase

import com.zekibiyikli.marticase.domain.model.LocationPoint
import com.zekibiyikli.marticase.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class ObserveLocationUpdatesUseCase(private val repository: LocationRepository) {
    operator fun invoke(): Flow<LocationPoint> = repository.locationUpdates()
}
