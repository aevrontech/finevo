package com.aevrontech.finevo.presentation.common

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/**
 * Android implementation of ImagePicker. Uses modern Photo Picker API for gallery and MediaStore
 * for camera.
 */
actual class ImagePicker {

    companion object {
        private const val TAG = "ImagePicker"

        // Store callback for activity result
        private var pendingCallback: ((ImagePickerResult) -> Unit)? = null
        private var tempPhotoUri: Uri? = null
        private var pendingContext: Context? = null

        // Activity result launchers - must be registered in Activity
        private var galleryLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
        private var cameraLauncher: ActivityResultLauncher<Uri>? = null
        private var cameraPermissionLauncher: ActivityResultLauncher<String>? = null

        /**
         * Register launchers in your Activity's onCreate. Call this before using ImagePicker
         * methods.
         */
        fun registerLaunchers(activity: ComponentActivity) {
            galleryLauncher =
                activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
                    handleGalleryResult(activity.contentResolver, uri)
                }

            cameraLauncher =
                activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
                    handleCameraResult(activity.contentResolver, success)
                }

            cameraPermissionLauncher =
                activity.registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean -> handleCameraPermissionResult(isGranted) }

            Log.d(TAG, "Launchers registered")
        }

        private fun handleGalleryResult(contentResolver: ContentResolver, uri: Uri?) {
            val callback = pendingCallback
            pendingCallback = null

            if (uri == null) {
                callback?.invoke(ImagePickerResult(null, "No image selected"))
                return
            }

            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    Log.d(TAG, "Gallery image loaded: ${bytes.size} bytes")
                    callback?.invoke(ImagePickerResult(bytes))
                } else {
                    callback?.invoke(ImagePickerResult(null, "Failed to read image"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read gallery image", e)
                callback?.invoke(ImagePickerResult(null, "Failed to read image: ${e.message}"))
            }
        }

        private fun handleCameraResult(contentResolver: ContentResolver, success: Boolean) {
            val callback = pendingCallback
            val photoUri = tempPhotoUri
            pendingCallback = null
            tempPhotoUri = null

            if (!success || photoUri == null) {
                callback?.invoke(ImagePickerResult(null, "Failed to capture photo"))
                return
            }

            try {
                val inputStream = contentResolver.openInputStream(photoUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    Log.d(TAG, "Camera image captured: ${bytes.size} bytes")
                    callback?.invoke(ImagePickerResult(bytes))
                } else {
                    callback?.invoke(ImagePickerResult(null, "Failed to read captured photo"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read camera image", e)
                callback?.invoke(ImagePickerResult(null, "Failed to read photo: ${e.message}"))
            }
        }

        private fun handleCameraPermissionResult(isGranted: Boolean) {
            val context = pendingContext
            val callback = pendingCallback

            if (!isGranted) {
                pendingCallback = null
                pendingContext = null
                callback?.invoke(ImagePickerResult(null, "Camera permission denied"))
                return
            }

            // Permission granted, launch camera
            if (context != null) {
                launchCameraInternal(context)
            } else {
                pendingCallback = null
                callback?.invoke(ImagePickerResult(null, "Context lost"))
            }
        }

        private fun launchCameraInternal(context: Context) {
            val launcher = cameraLauncher
            if (launcher == null) {
                val callback = pendingCallback
                pendingCallback = null
                pendingContext = null
                callback?.invoke(
                    ImagePickerResult(
                        null,
                        "ImagePicker not initialized. Call registerLaunchers first."
                    )
                )
                return
            }

            val photoFile = createTempImageFile(context)
            if (photoFile == null) {
                val callback = pendingCallback
                pendingCallback = null
                pendingContext = null
                callback?.invoke(ImagePickerResult(null, "Failed to create temp file"))
                return
            }

            val photoUri =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )

            tempPhotoUri = photoUri

            try {
                launcher.launch(photoUri)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch camera", e)
                val callback = pendingCallback
                pendingCallback = null
                pendingContext = null
                tempPhotoUri = null
                callback?.invoke(ImagePickerResult(null, "Failed to open camera: ${e.message}"))
            }
        }

        private fun createTempImageFile(context: Context): File? {
            return try {
                val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                File.createTempFile("PROFILE_", ".jpg", storageDir)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to create temp file", e)
                null
            }
        }
    }

    actual fun pickFromGallery(context: Any, onResult: (ImagePickerResult) -> Unit) {
        if (context !is Activity) {
            onResult(ImagePickerResult(null, "Invalid context"))
            return
        }

        val launcher = galleryLauncher
        if (launcher == null) {
            onResult(
                ImagePickerResult(
                    null,
                    "ImagePicker not initialized. Call registerLaunchers first."
                )
            )
            return
        }

        pendingCallback = onResult

        try {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch gallery picker", e)
            pendingCallback = null
            onResult(ImagePickerResult(null, "Failed to open gallery: ${e.message}"))
        }
    }

    actual fun captureFromCamera(context: Any, onResult: (ImagePickerResult) -> Unit) {
        if (context !is Activity) {
            onResult(ImagePickerResult(null, "Invalid context"))
            return
        }

        val permissionLauncher = cameraPermissionLauncher
        if (permissionLauncher == null) {
            onResult(
                ImagePickerResult(
                    null,
                    "ImagePicker not initialized. Call registerLaunchers first."
                )
            )
            return
        }

        pendingCallback = onResult
        pendingContext = context

        // Check if camera permission is already granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, launch camera directly
            launchCameraInternal(context)
        } else {
            // Request camera permission
            Log.d(TAG, "Requesting camera permission")
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
