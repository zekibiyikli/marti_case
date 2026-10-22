package com.zekibiyikli.marticase.domain.usecase

import com.zekibiyikli.marticase.domain.model.MapStyle
import com.zekibiyikli.marticase.domain.repository.SettingsRepository

class SetMapStyleUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(style: MapStyle) = repository.setMapStyle(style)
}
