package com.aevrontech.finevo.data.local

import android.content.Context

/**
 * Android implementation of SecureStorage using EncryptionKeyManager. This effectively uses
 * EncryptedSharedPreferences backed by Android Keystore.
 */
class AndroidSecureStorage(private val context: Context) : SecureStorage {
    override fun saveSecret(key: String, value: String) {
        EncryptionKeyManager.saveSecret(context, key, value)
    }

    override fun getSecret(key: String): String? {
        return EncryptionKeyManager.getSecret(context, key)
    }

    override fun removeSecret(key: String) {
        EncryptionKeyManager.removeSecret(context, key)
    }
}
