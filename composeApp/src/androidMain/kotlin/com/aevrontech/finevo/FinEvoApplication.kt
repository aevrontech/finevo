package com.aevrontech.finevo

import android.app.Application
import com.aevrontech.finevo.data.remote.SupabaseConfig
import com.aevrontech.finevo.di.appModule
import com.aevrontech.finevo.di.platformModule
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class FinEvoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val appStart = System.currentTimeMillis()
        android.util.Log.i("StartupTiming", "▶ Application.onCreate START")

        // Initialize Koin first (required for DI)
        val koinStart = System.currentTimeMillis()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@FinEvoApplication)
            modules(platformModule(), appModule)
        }
        android.util.Log.i(
            "StartupTiming",
            "  Koin init: ${System.currentTimeMillis() - koinStart}ms"
        )

        // Initialize Supabase in background - NOT blocking startup
        // Auth will be available when needed (login/signup)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val supabaseStart = System.currentTimeMillis()
            initializeSupabase()
            android.util.Log.i(
                "StartupTiming",
                "  Supabase init (background): ${System.currentTimeMillis() - supabaseStart}ms"
            )
        }

        android.util.Log.i(
            "StartupTiming",
            "◀ Application.onCreate END: ${System.currentTimeMillis() - appStart}ms"
        )
    }

    private fun initializeSupabase() {
        try {
            val supabaseUrl = BuildConfig.SUPABASE_URL
            val supabaseKey = BuildConfig.SUPABASE_ANON_KEY

            if (supabaseUrl.isNotEmpty() && supabaseKey.isNotEmpty()) {
                val secureStorage = com.aevrontech.finevo.data.local.AndroidSecureStorage(this)
                SupabaseConfig.initialize(supabaseUrl, supabaseKey, secureStorage)
            }
        } catch (e: Exception) {
            // Supabase not configured - app will work in offline mode
            android.util.Log.w("FinEvo", "Supabase not configured: ${e.message}")
        }
    }
}
