package com.aevrontech.finevo

import android.app.Application
import com.aevrontech.finevo.data.remote.SupabaseConfig
import com.aevrontech.finevo.di.appModule
import com.aevrontech.finevo.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class FinEvoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@FinEvoApplication)
            modules(platformModule(), appModule)
        }
        
        // Initialize Supabase
        initializeSupabase()
    }
    
    private fun initializeSupabase() {
        try {
            val supabaseUrl = BuildConfig.SUPABASE_URL
            val supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            
            if (supabaseUrl.isNotEmpty() && supabaseKey.isNotEmpty()) {
                SupabaseConfig.initialize(supabaseUrl, supabaseKey)
            }
        } catch (e: Exception) {
            // Supabase not configured - app will work in offline mode
            android.util.Log.w("FinEvo", "Supabase not configured: ${e.message}")
        }
    }
}
