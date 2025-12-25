package com.aevrontech.finevo.core.util

/**
 * Platform detection utilities.
 */
expect object Platform {
    val isAndroid: Boolean
    val isIOS: Boolean
    val name: String
}
