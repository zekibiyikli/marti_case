package com.zekibiyikli.marticase.di

import com.zekibiyikli.marticase.domain.usecase.AddMarkerUseCase
import com.zekibiyikli.marticase.domain.usecase.ClearMarkersUseCase
import com.zekibiyikli.marticase.domain.usecase.GetAddressUseCase
import com.zekibiyikli.marticase.domain.usecase.ObserveLocationUpdatesUseCase
import com.zekibiyikli.marticase.domain.usecase.ObserveMapStyleUseCase
import com.zekibiyikli.marticase.domain.usecase.ObserveMarkersUseCase
import com.zekibiyikli.marticase.domain.usecase.SetMapStyleUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::ObserveLocationUpdatesUseCase)
    factoryOf(::GetAddressUseCase)
    factoryOf(::ObserveMarkersUseCase)
    factoryOf(::AddMarkerUseCase)
    factoryOf(::ClearMarkersUseCase)
    factoryOf(::ObserveMapStyleUseCase)
    factoryOf(::SetMapStyleUseCase)
}
