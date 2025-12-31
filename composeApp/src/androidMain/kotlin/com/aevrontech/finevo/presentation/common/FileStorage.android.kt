package com.aevrontech.finevo.presentation.common

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

actual object FileStorage {
    private const val TAG = "FileStorage"
    private const val AVATAR_DIR = "avatars"

    actual fun saveAvatar(context: Any, bytes: ByteArray, fileName: String): String? {
        if (context !is Context) {
            Log.e(TAG, "Invalid context type")
            return null
        }

        return try {
            // Create avatars directory in app's private storage
            val avatarsDir = File(context.filesDir, AVATAR_DIR)
            if (!avatarsDir.exists()) {
                avatarsDir.mkdirs()
            }

            // Create file with unique name
            val file = File(avatarsDir, fileName)

            // Write bytes to file
            FileOutputStream(file).use { fos -> fos.write(bytes) }

            Log.d(TAG, "Avatar saved to: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save avatar", e)
            null
        }
    }
}
