package com.aevrontech.finevo.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * iOS implementation of DatabaseFactory.
 * Uses native SQLite driver.
 * TODO: Add SQLCipher support for iOS
 */
actual class DatabaseFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = FinEvoDatabase.Schema,
            name = "finevo.db"
        )
    }
}
