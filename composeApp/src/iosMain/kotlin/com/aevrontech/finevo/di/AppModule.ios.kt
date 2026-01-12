package com.aevrontech.finevo.di

import com.aevrontech.finevo.data.local.DatabaseFactory
import org.koin.dsl.module

/** iOS-specific dependency injection module. */
actual fun platformModule() = module {
    single { DatabaseFactory() }
    single<com.aevrontech.finevo.data.local.SecureStorage> {
        com.aevrontech.finevo.data.local.IosSecureStorage()
    }
    single<com.aevrontech.finevo.domain.manager.NotificationManager> {
        com.aevrontech.finevo.data.manager.IosNotificationManager()
    }
    single<com.aevrontech.finevo.domain.manager.BiometricManager> {
        com.aevrontech.finevo.data.manager.IosBiometricManager()
    }
}
