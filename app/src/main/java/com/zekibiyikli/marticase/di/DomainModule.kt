package com.zekibiyikli.marticase.di

import com.zekibiyikli.marticase.domain.usecase.GetAddressUseCase
import com.zekibiyikli.marticase.domain.usecase.ObserveLocationUpdatesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::ObserveLocationUpdatesUseCase)
    factoryOf(::GetAddressUseCase)
}
