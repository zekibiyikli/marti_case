package com.zekibiyikli.marticase.domain.usecase

import com.zekibiyikli.marticase.domain.repository.MarkerRepository

class ClearMarkersUseCase(private val repository: MarkerRepository) {
    suspend operator fun invoke() = repository.clearMarkers()
}
