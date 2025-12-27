package com.aevrontech.finevo.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * Supabase client configuration.
 * Credentials are loaded from BuildConfig (injected from local.properties).
 */
object SupabaseConfig {

    private var _client: SupabaseClient? = null

    /**
     * Initialize Supabase client with URL and key.
     * Call this during app initialization.
     */
    fun initialize(supabaseUrl: String, supabaseKey: String) {
        if (_client != null) return

        _client = createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Auth) {
                flowType = FlowType.PKCE
                scheme = "finevo"
                host = "auth-callback"
            }
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    /**
     * Get the Supabase client instance.
     * Throws if not initialized.
     */
    val client: SupabaseClient
        get() = _client ?: throw IllegalStateException(
            "Supabase client not initialized. Call SupabaseConfig.initialize() first."
        )

    /**
     * Check if client is initialized.
     */
    val isInitialized: Boolean
        get() = _client != null
}
