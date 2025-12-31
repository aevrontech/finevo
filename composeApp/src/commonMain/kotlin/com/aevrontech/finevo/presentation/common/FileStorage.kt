package com.aevrontech.finevo.presentation.common

/** Platform-specific file storage for saving avatar images locally */
expect object FileStorage {
    /**
     * Save image bytes to a local file in the app's private storage
     * @param bytes The image data to save
     * @param fileName The name of the file (without path)
     * @return The absolute path to the saved file, or null if failed
     */
    fun saveAvatar(context: Any, bytes: ByteArray, fileName: String): String?
}
