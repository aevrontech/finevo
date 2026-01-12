package com.aevrontech.finevo

import com.aevrontech.finevo.data.remote.SupabaseConfig
import com.aevrontech.finevo.di.appModule
import com.aevrontech.finevo.di.platformModule
import org.koin.core.context.startKoin

/**
 * Helper class for iOS to initialize dependencies. This should be called from your Swift App struct
 * / AppDelegate.
 */
fun initKoin() {
    startKoin { modules(platformModule(), appModule) }
}

/** Initialize Supabase on iOS. Call this after initKoin() from Swift. */
fun initSupabase() {
    // TODO: Replace these with proper build config fields or a secure way to inject them from iOS
    val supabaseUrl = "YOUR_SUPABASE_URL"
    val supabaseKey = "YOUR_SUPABASE_KEY"

    if (supabaseUrl != "YOUR_SUPABASE_URL") {
        val secureStorage = com.aevrontech.finevo.data.local.IosSecureStorage()
        SupabaseConfig.initialize(supabaseUrl, supabaseKey, secureStorage)
    }
}
