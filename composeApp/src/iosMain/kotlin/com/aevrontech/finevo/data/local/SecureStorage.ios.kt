package com.aevrontech.finevo.data.local

/**
 * iOS implementation of SecureStorage. TODO: Implement using Keychain (e.g., using a library like
 * ksecurity or multiplatform-settings-keychain). For now, this is a stub or could use
 * NSUserDefaults (INSECURE - DO NOT USE FOR REAL SECRETS YET). Marking as TODO to ensure it's
 * implemented properly directly in iOS phase.
 */
class IosSecureStorage : SecureStorage {
    // TODO: proper keychain implementation
    private val memoryCache = mutableMapOf<String, String>()

    override fun saveSecret(key: String, value: String) {
        memoryCache[key] = value
        // TODO: Save to Keychain
    }

    override fun getSecret(key: String): String? {
        return memoryCache[key]
        // TODO: Read from Keychain
    }

    override fun removeSecret(key: String) {
        memoryCache.remove(key)
        // TODO: Remove from Keychain
    }
}
