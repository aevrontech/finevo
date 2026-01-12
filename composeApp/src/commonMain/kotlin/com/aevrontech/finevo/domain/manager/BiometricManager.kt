package com.aevrontech.finevo.domain.manager

interface BiometricManager {
    /** Check if biometric authentication is available on the device */
    fun canAuthenticate(): Boolean

    /**
     * Authenticate using biometrics
     * @param title Title of the prompt
     * @param subtitle Subtitle of the prompt
     * @return true if successful, false otherwise
     */
    suspend fun authenticate(title: String, subtitle: String): Boolean
}
