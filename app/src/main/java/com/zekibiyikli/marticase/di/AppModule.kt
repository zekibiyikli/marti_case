package com.zekibiyikli.marticase.di

import com.zekibiyikli.marticase.service.RouteTracker
import org.koin.dsl.module

val appModule = module {
    single { RouteTracker(get(), get(), get(), get()) }
}

val appModules = listOf(appModule, dataModule, domainModule, presentationModule)
