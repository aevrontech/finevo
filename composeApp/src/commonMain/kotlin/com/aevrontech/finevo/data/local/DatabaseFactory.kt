package com.aevrontech.finevo.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform-specific database driver factory.
 * Each platform provides its own implementation.
 */
expect class DatabaseFactory {
    fun createDriver(): SqlDriver
}

/**
 * Creates the FinEvoDatabase instance using the platform-specific driver.
 */
fun createDatabase(driverFactory: DatabaseFactory): FinEvoDatabase {
    val driver = driverFactory.createDriver()
    return FinEvoDatabase(driver)
}
