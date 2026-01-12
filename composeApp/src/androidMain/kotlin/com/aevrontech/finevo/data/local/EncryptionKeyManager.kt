package com.aevrontech.finevo.data.local

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Manages the encryption key for the local database using Android's EncryptedSharedPreferences. The
 * key is generated once and stored securely using the Android Keystore system.
 */
object EncryptionKeyManager {
    private const val PREFS_FILE_NAME = "finevo_secure_prefs"
    private const val KEY_DATABASE_PASSWORD = "database_password"
    private const val KEY_SIZE_BYTES = 32

    private fun getPrefs(context: Context): android.content.SharedPreferences {
        val masterKey =
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Gets or creates the database encryption key. */
    fun getOrCreateKey(context: Context): ByteArray {
        val prefs = getPrefs(context)

        // Check if key already exists
        val existingKeyBase64 = prefs.getString(KEY_DATABASE_PASSWORD, null)
        if (existingKeyBase64 != null) {
            return Base64.decode(existingKeyBase64, Base64.DEFAULT)
        }

        // Generate new random key
        val newKey = ByteArray(KEY_SIZE_BYTES)
        SecureRandom().nextBytes(newKey)

        // Store secure key
        val newKeyBase64 = Base64.encodeToString(newKey, Base64.DEFAULT)
        prefs.edit { putString(KEY_DATABASE_PASSWORD, newKeyBase64) }

        return newKey
    }

    /** Saves a general secret (e.g., Auth Token) securely. */
    fun saveSecret(context: Context, key: String, value: String) {
        getPrefs(context).edit { putString(key, value) }
    }

    /** Retrieves a general secret securely. */
    fun getSecret(context: Context, key: String): String? {
        return getPrefs(context).getString(key, null)
    }

    /** Removes a stored secret. */
    fun removeSecret(context: Context, key: String) {
        getPrefs(context).edit { remove(key) }
    }
}
