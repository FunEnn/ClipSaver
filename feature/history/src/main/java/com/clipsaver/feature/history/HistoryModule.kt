package com.clipsaver.feature.history

import org.koin.dsl.module

val module = module {
    single { HistoryViewModel() }
}