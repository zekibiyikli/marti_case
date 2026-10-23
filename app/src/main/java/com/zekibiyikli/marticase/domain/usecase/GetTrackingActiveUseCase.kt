package com.zekibiyikli.marticase.domain.usecase

import com.zekibiyikli.marticase.domain.repository.SettingsRepository

class GetTrackingActiveUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): Boolean = repository.isTrackingActive()
}
