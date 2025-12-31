package com.aevrontech.finevo.presentation.common

/** Result from image picker operations. */
data class ImagePickerResult(val bytes: ByteArray?, val error: String? = null) {
    val isSuccess: Boolean
        get() = bytes != null && error == null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ImagePickerResult
        if (bytes != null) {
            if (other.bytes == null) return false
            if (!bytes.contentEquals(other.bytes)) return false
        } else if (other.bytes != null) return false
        if (error != other.error) return false
        return true
    }

    override fun hashCode(): Int {
        var result = bytes?.contentHashCode() ?: 0
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}

/** Platform-specific image picker interface. Each platform implements gallery and camera access. */
expect class ImagePicker() {
    /**
     * Pick an image from the device gallery.
     * @param context Platform-specific context (Activity on Android)
     * @param onResult Called with the image bytes or error
     */
    fun pickFromGallery(context: Any, onResult: (ImagePickerResult) -> Unit)

    /**
     * Capture a photo using the device camera.
     * @param context Platform-specific context (Activity on Android)
     * @param onResult Called with the image bytes or error
     */
    fun captureFromCamera(context: Any, onResult: (ImagePickerResult) -> Unit)
}
