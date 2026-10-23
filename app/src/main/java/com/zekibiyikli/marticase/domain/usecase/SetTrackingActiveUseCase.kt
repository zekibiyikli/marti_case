package com.zekibiyikli.marticase.domain.usecase

import com.zekibiyikli.marticase.domain.repository.SettingsRepository

class SetTrackingActiveUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(active: Boolean) = repository.setTrackingActive(active)
}
