package com.aevrontech.finevo.presentation.auth

/**
 * Expect class for platform-specific social login handling. Each platform implements the actual
 * social login SDK integration.
 */
expect class SocialLoginHandler() {
        /**
         * Initiate Google Sign-In flow.
         * @param activity Platform-specific activity/context (Activity on Android, UIViewController
         * on iOS)
         * @param onSuccess Called with the ID token AND nonce on successful sign-in
         * @param onError Called with error message on failure
         */
        suspend fun signInWithGoogle(
                activity: Any,
                onSuccess: (idToken: String, nonce: String?) -> Unit,
                onError: (message: String) -> Unit
        )

        /**
         * Initiate Apple Sign-In flow.
         * @param activity Platform-specific activity/context
         * @param onSuccess Called with ID token and nonce on successful sign-in
         * @param onError Called with error message on failure
         */
        suspend fun signInWithApple(
                activity: Any,
                onSuccess: (idToken: String, nonce: String) -> Unit,
                onError: (message: String) -> Unit
        )

        /** Check if Google Sign-In is available on this platform. */
        fun isGoogleSignInAvailable(): Boolean

        /** Check if Apple Sign-In is available on this platform. */
        fun isAppleSignInAvailable(): Boolean
}
