package com.aevrontech.finevo.ui.theme

import platform.Foundation.NSUserDefaults

/** iOS implementation of dark mode preference storage */
private const val KEY_DARK_MODE = "dark_mode"

actual fun saveDarkModePreference(enabled: Boolean) {
    NSUserDefaults.standardUserDefaults.setBool(enabled, KEY_DARK_MODE)
}

actual fun loadDarkModePreference(): Boolean {
    return NSUserDefaults.standardUserDefaults.boolForKey(KEY_DARK_MODE)
}
