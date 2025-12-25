package com.aevrontech.finevo.presentation.auth

import kotlin.random.Random

/**
 * iOS implementation of SocialLoginHandler. Note: Full Apple Sign-In implementation requires
 * ASAuthorizationController setup.
 */
actual class SocialLoginHandler {

    actual suspend fun signInWithGoogle(
            activity: Any,
            onSuccess: (idToken: String, nonce: String?) -> Unit,
            onError: (message: String) -> Unit
    ) {
        // On iOS, Google Sign-In requires the GoogleSignIn SDK
        onError("Google Sign-In requires additional setup. Please use Apple Sign-In on iOS.")
    }

    actual suspend fun signInWithApple(
            activity: Any,
            onSuccess: (idToken: String, nonce: String) -> Unit,
            onError: (message: String) -> Unit
    ) {
        // TODO: Implement using ASAuthorizationController
        onError("Apple Sign-In setup in progress. Please use email sign-in.")
    }

    actual fun isGoogleSignInAvailable(): Boolean {
        return false
    }

    actual fun isAppleSignInAvailable(): Boolean {
        return true
    }

    private fun generateNonce(length: Int = 32): String {
        val charset = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length).map { charset[Random.nextInt(charset.size)] }.joinToString("")
    }
}
