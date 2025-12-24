package com.aevrontech.finevo.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android implementation of DatabaseFactory.
 * Uses standard SQLite driver for now.
 * TODO: Add SQLCipher for encryption in production.
 */
actual class DatabaseFactory(
    private val context: Context
) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = FinEvoDatabase.Schema,
            context = context,
            name = "finevo.db"
        )
    }
}
