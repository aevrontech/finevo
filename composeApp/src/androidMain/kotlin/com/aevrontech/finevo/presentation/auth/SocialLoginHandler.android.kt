package com.aevrontech.finevo.presentation.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

/**
 * Android implementation of SocialLoginHandler. Uses Credential Manager first, falls back to legacy
 * Google Sign-In.
 */
actual class SocialLoginHandler {

    companion object {
        private const val TAG = "SocialLoginHandler"
        const val RC_SIGN_IN = 9001

        // Store callbacks for activity result handling (legacy)
        private var pendingOnSuccess: ((String, String?) -> Unit)? = null
        private var pendingOnError: ((String) -> Unit)? = null

        /**
         * Handle activity result from legacy Google Sign-In. Call this from your Activity's
         * onActivityResult.
         */
        fun handleActivityResult(requestCode: Int, data: Intent?) {
            if (requestCode == RC_SIGN_IN) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }

        private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
            try {
                val account = completedTask.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    Log.d(TAG, "Legacy sign-in success, got ID token")
                    // Legacy doesn't use nonce, pass null
                    pendingOnSuccess?.invoke(idToken, null)
                } else {
                    Log.e(TAG, "No ID token in account")
                    pendingOnError?.invoke("No ID token received from Google")
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Legacy sign-in failed: ${e.statusCode}", e)
                val message =
                    when (e.statusCode) {
                        12501 -> "Sign-in was cancelled"
                        12502 -> "Sign-in failed. Please try again."
                        10 -> "Developer error. Check your configuration."
                        else -> "Google Sign-In failed (${e.statusCode})"
                    }
                pendingOnError?.invoke(message)
            } finally {
                pendingOnSuccess = null
                pendingOnError = null
            }
        }
    }

    // Get Web Client ID from BuildConfig
    private val googleWebClientId: String
        get() =
            try {
                com.aevrontech.finevo.BuildConfig.GOOGLE_WEB_CLIENT_ID
            } catch (e: Exception) {
                ""
            }

    private var googleSignInClient: GoogleSignInClient? = null

    // Store the raw nonce to pass to Supabase
    private var currentRawNonce: String? = null

    /**
     * Sign in with Google - tries Credential Manager first, falls back to legacy. Returns both ID
     * token and nonce (needed for Supabase).
     */
    actual suspend fun signInWithGoogle(
        activity: Any,
        onSuccess: (idToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (activity !is Activity) {
            onError("Invalid context. Activity required for Google Sign-In.")
            return
        }

        if (googleWebClientId.isBlank()) {
            onError("Google Sign-In not configured. Add GOOGLE_WEB_CLIENT_ID to local.properties.")
            return
        }

        Log.d(TAG, "Starting Google Sign-In...")
        Log.d(TAG, "Client ID: ${googleWebClientId.take(30)}...")

        // Try Credential Manager first (modern approach)
        try {
            signInWithCredentialManager(activity, onSuccess, onError)
        } catch (e: NoCredentialException) {
            Log.w(TAG, "Credential Manager failed, trying legacy sign-in...")
            signInWithLegacy(activity, onSuccess, onError)
        } catch (e: Exception) {
            Log.w(TAG, "Credential Manager error: ${e.message}, trying legacy...")
            signInWithLegacy(activity, onSuccess, onError)
        }
    }

    private suspend fun signInWithCredentialManager(
        activity: Activity,
        onSuccess: (idToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            val credentialManager = CredentialManager.create(activity)

            // Generate nonce for security
            val rawNonce = UUID.randomUUID().toString()
            currentRawNonce = rawNonce // Store for passing to Supabase

            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            Log.d(TAG, "Generated nonce (raw): ${rawNonce.take(20)}...")
            Log.d(TAG, "Hashed nonce: ${hashedNonce.take(20)}...")

            val googleIdOption =
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(googleWebClientId)
                    .setAutoSelectEnabled(false)
                    .setNonce(hashedNonce) // Send hashed nonce to Google
                    .build()

            val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

            Log.d(TAG, "Requesting credentials with Credential Manager...")

            val result = credentialManager.getCredential(request = request, context = activity)
            handleCredentialResult(result, rawNonce, onSuccess, onError)
        }
    }

    private fun signInWithLegacy(
        activity: Activity,
        onSuccess: (idToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        Log.d(TAG, "Using legacy Google Sign-In (no nonce)...")

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(googleWebClientId)
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)

        // Store callbacks for activity result
        pendingOnSuccess = onSuccess
        pendingOnError = onError

        // Sign out first to ensure account picker shows
        googleSignInClient?.signOut()?.addOnCompleteListener {
            val signInIntent = googleSignInClient?.signInIntent
            if (signInIntent != null) {
                activity.startActivityForResult(signInIntent, RC_SIGN_IN)
            } else {
                onError("Failed to create sign-in intent")
            }
        }
    }

    private fun handleCredentialResult(
        result: GetCredentialResponse,
        rawNonce: String,
        onSuccess: (idToken: String, nonce: String?) -> Unit,
        onError: (message: String) -> Unit
    ) {
        Log.d(TAG, "Handling credential result...")

        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                        Log.d(TAG, "Successfully got Google ID token")
                        Log.d(TAG, "Passing raw nonce to Supabase: ${rawNonce.take(20)}...")
                        // Pass both ID token AND the raw nonce
                        onSuccess(googleIdToken.idToken, rawNonce)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Failed to parse token", e)
                        onError("Failed to parse Google ID token")
                    }
                } else {
                    onError("Unexpected credential type: ${credential.type}")
                }
            }

            else -> {
                onError("Unexpected credential type")
            }
        }
    }

    actual suspend fun signInWithApple(
        activity: Any,
        onSuccess: (idToken: String, nonce: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        onError("Apple Sign-In is only available on iOS devices")
    }

    actual fun isGoogleSignInAvailable(): Boolean {
        return googleWebClientId.isNotBlank()
    }

    actual fun isAppleSignInAvailable(): Boolean {
        return false
    }
}
