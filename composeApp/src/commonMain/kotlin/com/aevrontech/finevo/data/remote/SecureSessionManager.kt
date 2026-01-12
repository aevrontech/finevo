package com.aevrontech.finevo.data.remote

import com.aevrontech.finevo.data.local.SecureStorage
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Supabase Session Manager that uses our [SecureStorage] to securely persist the authentication
 * session (tokens) instead of plain-text preferences.
 */
class SecureSessionManager(private val secureStorage: SecureStorage) : SessionManager {

    override suspend fun saveSession(session: UserSession) {
        val json = Json.encodeToString(session)
        secureStorage.saveSecret("supabase_session", json)
    }

    override suspend fun loadSession(): UserSession? {
        val json = secureStorage.getSecret("supabase_session") ?: return null
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteSession() {
        secureStorage.removeSecret("supabase_session")
    }
}
