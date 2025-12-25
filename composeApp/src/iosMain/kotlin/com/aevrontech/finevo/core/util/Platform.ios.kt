package com.aevrontech.finevo.core.util

actual object Platform {
    actual val isAndroid: Boolean = false
    actual val isIOS: Boolean = true
    actual val name: String = "iOS"
}
