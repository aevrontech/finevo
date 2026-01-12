package com.aevrontech.finevo.data.manager

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.aevrontech.finevo.domain.manager.BiometricManager
import com.aevrontech.finevo.util.ActivityProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.biometric.BiometricManager as AndroidBiometricManagerCompat

class AndroidBiometricManager(private val context: Context) : BiometricManager {

    override fun canAuthenticate(): Boolean {
        val biometricManager = AndroidBiometricManagerCompat.from(context)
        return when (biometricManager.canAuthenticate(
            AndroidBiometricManagerCompat.Authenticators.BIOMETRIC_STRONG
        )
        ) {
            AndroidBiometricManagerCompat.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    override suspend fun authenticate(title: String, subtitle: String): Boolean {
        val activity = ActivityProvider.currentActivity as? FragmentActivity ?: return false

        return suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(context)
            val callback =
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        if (continuation.isActive) {
                            continuation.resume(true)
                        }
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        // If user cancels (errorCode 13 or 10), we resume with false
                        // If hardware unavailable, also false
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        // Biometric is valid but not recognized. The prompt stays open, so we
                        // don't resume yet.
                    }
                }

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            val promptInfo =
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setAllowedAuthenticators(
                        AndroidBiometricManagerCompat.Authenticators.BIOMETRIC_STRONG
                    )
                    .setNegativeButtonText("Cancel")
                    .build()

            biometricPrompt.authenticate(promptInfo)

            continuation.invokeOnCancellation { biometricPrompt.cancelAuthentication() }
        }
    }
}
