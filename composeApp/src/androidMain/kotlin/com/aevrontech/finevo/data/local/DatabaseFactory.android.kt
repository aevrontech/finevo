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
                val driver =
                        AndroidSqliteDriver(
                                schema = FinEvoDatabase.Schema,
                                context = context,
                                name = "finevo.db"
                        )

                // Ensure tables exist for databases created before updates
                ensureAccountsTableExists(driver)
                ensureDefaultCategoriesExist(driver)

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

                driver.execute(
                        null,
                        "CREATE INDEX IF NOT EXISTS idx_accounts_user ON accounts(user_id)",
                        0
                )
                driver.execute(
                        null,
                        "CREATE INDEX IF NOT EXISTS idx_accounts_active ON accounts(is_active)",
                        0
                )
        }

        private fun ensureDefaultCategoriesExist(driver: SqlDriver) {
                // Insert default categories if they don't exist (INSERT OR IGNORE handles
                // duplicates)
                val now = System.currentTimeMillis()

                // Expense categories
                val expenseCategories =
                        listOf(
                                Triple("cat_food", "Food & Dining", "🍔" to "#FF5252"),
                                Triple("cat_transport", "Transportation", "🚗" to "#FF9800"),
                                Triple("cat_shopping", "Shopping", "🛍️" to "#E91E63"),
                                Triple("cat_bills", "Bills & Utilities", "📄" to "#9C27B0"),
                                Triple("cat_entertainment", "Entertainment", "🎬" to "#673AB7"),
                                Triple("cat_health", "Health", "💊" to "#4CAF50"),
                                Triple("cat_education", "Education", "📚" to "#2196F3"),
                                Triple("cat_other_exp", "Other", "📦" to "#607D8B")
                        )

                expenseCategories.forEachIndexed { index, (id, name, iconColor) ->
                        driver.execute(
                                null,
                                """
                    INSERT OR IGNORE INTO categories (id, user_id, name, icon, color, type, is_default, sort_order, created_at)
                    VALUES ('$id', NULL, '$name', '${iconColor.first}', '${iconColor.second}', 'EXPENSE', 1, $index, $now)
                    """.trimIndent(),
                                0
                        )
                }

                // Income categories
                val incomeCategories =
                        listOf(
                                Triple("cat_salary", "Salary", "💰" to "#4CAF50"),
                                Triple("cat_freelance", "Freelance", "💼" to "#8BC34A"),
                                Triple("cat_investment", "Investment", "📈" to "#00BCD4"),
                                Triple("cat_gift", "Gift", "🎁" to "#FF4081"),
                                Triple("cat_other_inc", "Other Income", "💵" to "#607D8B")
                        )

                incomeCategories.forEachIndexed { index, (id, name, iconColor) ->
                        driver.execute(
                                null,
                                """
                    INSERT OR IGNORE INTO categories (id, user_id, name, icon, color, type, is_default, sort_order, created_at)
                    VALUES ('$id', NULL, '$name', '${iconColor.first}', '${iconColor.second}', 'INCOME', 1, $index, $now)
                    """.trimIndent(),
                                0
                        )
                }
        }
}
