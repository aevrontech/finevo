package com.aevrontech.finevo.data.manager

import com.aevrontech.finevo.domain.manager.BiometricManager
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlin.coroutines.resume

class IosBiometricManager : BiometricManager {
    override fun canAuthenticate(): Boolean {
        val context = LAContext()
        // Pass null for error pointer if we just want the boolean result
        return context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)
    }

    override suspend fun authenticate(title: String, subtitle: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val context = LAContext()
            context.localizedFallbackTitle = "" // Disable fallback button if desired

            // The reason string is displayed to the user
            val reason = "$title\n$subtitle"

            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = reason
            ) { success, error ->
                // This callback runs on a background thread
                if (continuation.isActive) {
                    continuation.resume(success)
                }
            }
        }
    }
}
