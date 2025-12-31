package com.aevrontech.finevo.presentation.common

/**
 * iOS implementation of ImagePicker. TODO: Implement using UIImagePickerController or
 * PHPickerViewController.
 */
actual class ImagePicker {

    actual fun pickFromGallery(context: Any, onResult: (ImagePickerResult) -> Unit) {
        // TODO: Implement iOS photo library access
        onResult(ImagePickerResult(null, "Photo picker not yet implemented for iOS"))
    }

    actual fun captureFromCamera(context: Any, onResult: (ImagePickerResult) -> Unit) {
        // TODO: Implement iOS camera access
        onResult(ImagePickerResult(null, "Camera not yet implemented for iOS"))
    }
}
