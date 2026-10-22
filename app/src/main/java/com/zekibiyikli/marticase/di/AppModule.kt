package com.zekibiyikli.marticase.di

import org.koin.dsl.module

val appModule = module {
}

val appModules = listOf(appModule, dataModule, domainModule, presentationModule)
