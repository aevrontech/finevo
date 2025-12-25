package com.aevrontech.finevo.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android implementation of DatabaseFactory. Uses standard SQLite driver for now. TODO: Add
 * SQLCipher for encryption in production.
 */
actual class DatabaseFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        // First, ensure the accounts table exists by running a migration check
        val driver =
                AndroidSqliteDriver(
                        schema = FinEvoDatabase.Schema,
                        context = context,
                        name = "finevo.db"
                )

        // Ensure accounts table exists for databases created before this update
        ensureAccountsTableExists(driver)

        return driver
    }

    private fun ensureAccountsTableExists(driver: SqlDriver) {
        driver.execute(
                null,
                """
            CREATE TABLE IF NOT EXISTS accounts (
                id TEXT NOT NULL PRIMARY KEY,
                user_id TEXT NOT NULL,
                name TEXT NOT NULL,
                balance REAL NOT NULL DEFAULT 0,
                currency TEXT NOT NULL DEFAULT 'MYR',
                type TEXT NOT NULL,
                color TEXT NOT NULL DEFAULT '#00D9FF',
                icon TEXT NOT NULL DEFAULT '💰',
                is_default INTEGER NOT NULL DEFAULT 0,
                is_active INTEGER NOT NULL DEFAULT 1,
                is_excluded_from_total INTEGER NOT NULL DEFAULT 0,
                sort_order INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """.trimIndent(),
                0
        )

        // Create indexes if they don't exist
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_accounts_user ON accounts(user_id)", 0)
        driver.execute(
                null,
                "CREATE INDEX IF NOT EXISTS idx_accounts_active ON accounts(is_active)",
                0
        )
    }
}
