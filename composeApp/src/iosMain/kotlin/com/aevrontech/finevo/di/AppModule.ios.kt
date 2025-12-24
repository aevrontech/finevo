package com.aevrontech.finevo.di

import com.aevrontech.finevo.data.local.DatabaseFactory
import org.koin.dsl.module

/**
 * iOS-specific dependency injection module.
 */
actual fun platformModule() = module {
    single { DatabaseFactory() }
}
