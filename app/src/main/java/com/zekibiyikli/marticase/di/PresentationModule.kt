package com.zekibiyikli.marticase.di

import com.zekibiyikli.marticase.presentation.map.MapViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::MapViewModel)
}
