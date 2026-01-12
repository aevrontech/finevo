package com.aevrontech.finevo.di

import com.aevrontech.finevo.data.local.DatabaseFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Android-specific dependency injection module. */
actual fun platformModule() = module {
    single { DatabaseFactory(androidContext()) }
    single<com.aevrontech.finevo.data.local.SecureStorage> {
        com.aevrontech.finevo.data.local.AndroidSecureStorage(androidContext())
    }
    single<com.aevrontech.finevo.domain.manager.NotificationManager> {
        com.aevrontech.finevo.data.manager.AndroidNotificationManager(androidContext())
    }
}
