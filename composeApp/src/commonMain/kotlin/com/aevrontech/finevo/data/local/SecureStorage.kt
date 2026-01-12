package com.aevrontech.finevo.data.local

/**
 * Interface for securely storing sensitive data (secrets) on the device. Implementations should use
 * platform-specific secure storage mechanisms (e.g., EncryptedSharedPreferences on Android,
 * Keychain on iOS).
 */
interface SecureStorage {
    /** Save a secret value associated with a key */
    fun saveSecret(key: String, value: String)

    /** Retrieve a secret value associated with a key */
    fun getSecret(key: String): String?

    /** Remove a secret value associated with a key */
    fun removeSecret(key: String)
}
