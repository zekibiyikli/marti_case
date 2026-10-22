package com.zekibiyikli.marticase.domain.usecase

import com.zekibiyikli.marticase.domain.model.LocationPoint
import com.zekibiyikli.marticase.domain.repository.GeocodingRepository

class GetAddressUseCase(private val repository: GeocodingRepository) {
    suspend operator fun invoke(point: LocationPoint): String? = repository.getAddress(point)
}
