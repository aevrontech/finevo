package com.aevrontech.finevo.ui.theme

import android.content.Context
import android.content.SharedPreferences

/** Android implementation of dark mode preference storage */
private var appContext: Context? = null
private const val PREFS_NAME = "finevo_theme_prefs"
private const val KEY_DARK_MODE = "dark_mode"

fun initThemePreferences(context: Context) {
    appContext = context.applicationContext
    ThemeManager.initialize()
}

private fun getPrefs(): SharedPreferences? {
    return appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

actual fun saveDarkModePreference(enabled: Boolean) {
    getPrefs()?.edit()?.putBoolean(KEY_DARK_MODE, enabled)?.apply()
}

actual fun loadDarkModePreference(): Boolean {
    return getPrefs()?.getBoolean(KEY_DARK_MODE, false) ?: false // Default to light mode
}
