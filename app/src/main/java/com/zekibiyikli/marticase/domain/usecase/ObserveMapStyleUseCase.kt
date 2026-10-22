package com.zekibiyikli.marticase.domain.usecase

import com.zekibiyikli.marticase.domain.model.MapStyle
import com.zekibiyikli.marticase.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveMapStyleUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<MapStyle> = repository.observeMapStyle()
}
