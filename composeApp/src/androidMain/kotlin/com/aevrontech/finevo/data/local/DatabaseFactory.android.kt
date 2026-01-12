package com.aevrontech.finevo.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * Android implementation of DatabaseFactory. Uses standard SQLite driver for now. TODO: Add
 * SQLCipher for encryption in production.
 */
actual class DatabaseFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        System.loadLibrary("sqlcipher")
        val passphrase = EncryptionKeyManager.getOrCreateKey(context)
        val factory = SupportOpenHelperFactory(passphrase)
        return AndroidSqliteDriver(
            schema = FinEvoDatabase.Schema,
            context = context,
            name = "finevo.db",
            factory = factory,
            callback =
                object : AndroidSqliteDriver.Callback(FinEvoDatabase.Schema) {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.setForeignKeyConstraintsEnabled(true)
                    }

                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        seedDefaultData(db)
                    }
                }
        )
    }

    private fun seedDefaultData(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            // 1. Default User
            db.execSQL(
                "INSERT OR IGNORE INTO users (id, email, display_name, created_at, updated_at) VALUES ('local_user', 'user@local', 'User', 0, 0)"
            )

            // 2. Expense Categories
            val expenseCategories =
                listOf(
                    Triple("cat_exp_accessories", "Accessories", "💍"),
                    Triple("cat_exp_beauty", "Beauty", "💄"),
                    Triple(
                        "cat_exp_bills_utilities",
                        "Bills & Utilities",
                        "📄"
                    ),
                    Triple("cat_exp_books", "Books", "📚"),
                    Triple("cat_exp_business", "Business", "💼"),
                    Triple("cat_exp_cafe", "Cafe", "☕"),
                    Triple("cat_exp_car", "Car", "🚗"),
                    Triple("cat_exp_charity", "Charity", "🎗️"),
                    Triple(
                        "cat_exp_children_babies",
                        "Children & Babies",
                        "👶"
                    ),
                    Triple("cat_exp_clothing", "Clothing", "👕"),
                    Triple("cat_exp_doctor", "Doctor", "👨‍⚕️"),
                    Triple("cat_exp_donations", "Donations", "🤝"),
                    Triple("cat_exp_education", "Education", "🎓"),
                    Triple(
                        "cat_exp_electricity_bills",
                        "Electricity Bills",
                        "⚡"
                    ),
                    Triple("cat_exp_electronics", "Electronics", "💻"),
                    Triple("cat_exp_entertainment", "Entertainment", "🎬"),
                    Triple("cat_exp_family", "Family", "👨‍👩‍👧‍👦"),
                    Triple("cat_exp_fees_charges", "Fees & Charges", "💸"),
                    Triple("cat_exp_food_beverage", "Food & Beverage", "🍔"),
                    Triple("cat_exp_footwear", "Footwear", "👟"),
                    Triple("cat_exp_friends", "Friends", "👯"),
                    Triple("cat_exp_funeral", "Funeral", "⚰️"),
                    Triple("cat_exp_games", "Games", "🎮"),
                    Triple("cat_exp_gas_bill", "Gas Bill", "⛽"),
                    Triple("cat_exp_gifts", "Gifts", "🎁"),
                    Triple("cat_exp_health_fitness", "Health & Fitness", "🏃"),
                    Triple(
                        "cat_exp_home_improvement",
                        "Home Improvement",
                        "🔨"
                    ),
                    Triple("cat_exp_home_services", "Home Services", "🧹"),
                    Triple("cat_exp_insurances", "Insurances", "🛡️"),
                    Triple("cat_exp_internet_bill", "Internet Bill", "🌐"),
                    Triple("cat_exp_investment", "Investment", "📈"),
                    Triple("cat_exp_kids", "Kids", "🧸"),
                    Triple("cat_exp_lover", "Lover", "💑"),
                    Triple("cat_exp_maintenance", "Maintenance", "🔧"),
                    Triple("cat_exp_marriage", "Marriage", "💍"),
                    Triple("cat_exp_movies", "Movies", "🍿"),
                    Triple("cat_exp_other_expenses", "Other expenses", "📦"),
                    Triple("cat_exp_parking_fees", "Parking fees", "🅿️"),
                    Triple("cat_exp_personal_care", "Personal Care", "🛁"),
                    Triple("cat_exp_petrol", "Petrol", "⛽"),
                    Triple("cat_exp_pets", "Pets", "🐾"),
                    Triple("cat_exp_pharmacy", "Pharmacy", "💊"),
                    Triple("cat_exp_phone", "Phone", "📱"),
                    Triple("cat_exp_rentals", "Rentals", "🏠"),
                    Triple("cat_exp_repairs", "Repairs", "🛠️"),
                    Triple("cat_exp_restaurants", "Restaurants", "🍽️"),
                    Triple("cat_exp_shopping", "Shopping", "🛍️"),
                    Triple("cat_exp_snacks", "Snacks", "🥨"),
                    Triple("cat_exp_social", "Social", "🥂"),
                    Triple("cat_exp_sports", "Sports", "⚽"),
                    Triple("cat_exp_taxi", "Taxi", "🚕"),
                    Triple("cat_exp_television_bill", "Television Bill", "📺"),
                    Triple("cat_exp_transportation", "Transportation", "🚌"),
                    Triple("cat_exp_travel", "Travel", "✈️"),
                    Triple("cat_exp_water_bill", "Water Bill", "💧"),
                    Triple("cat_exp_withdrawal", "Withdrawal", "🏧")
                )

            // Map icon to color (simplified logic for now, using existing mapping)
            val colorMap =
                mapOf(
                    "Food & Beverage" to "#FF5252",
                    "Transportation" to "#FF9800",
                    "Shopping" to "#E91E63",
                    "Bills & Utilities" to "#9C27B0",
                    "Entertainment" to "#673AB7",
                    "Health & Fitness" to "#4CAF50",
                    "Car" to "#2196F3",
                    "Education" to "#2196F3",
                    "Housing" to "#009688",
                    "Investment" to "#4CAF50"
                )
            val colors =
                listOf(
                    "#E91E63",
                    "#9C27B0",
                    "#673AB7",
                    "#3F51B5",
                    "#2196F3",
                    "#03A9F4",
                    "#00BCD4",
                    "#009688",
                    "#4CAF50",
                    "#8BC34A",
                    "#CDDC39",
                    "#FFEB3B",
                    "#FFC107",
                    "#FF9800",
                    "#FF5722",
                    "#795548",
                    "#9E9E9E",
                    "#607D8B"
                )

            expenseCategories.forEachIndexed { index, (id, name, icon) ->
                val color = colorMap[name] ?: colors[index % colors.size]
                db.execSQL(
                    "INSERT OR IGNORE INTO categories (id, user_id, name, icon, color, type, is_default, sort_order, created_at) VALUES ('$id', 'local_user', '$name', '$icon', '$color', 'EXPENSE', 1, $index, 0)"
                )
            }

            // 3. Income Categories
            val incomeCategories =
                listOf(
                    Triple("cat_inc_award", "Award", "🏆"),
                    Triple("cat_inc_bonus", "Bonus", "💰"),
                    Triple("cat_inc_business", "Business", "💼"),
                    Triple("cat_inc_gifts", "Gifts", "🎁"),
                    Triple("cat_inc_income", "Income", "💵"),
                    Triple("cat_inc_investment", "Investment", "📈"),
                    Triple("cat_inc_others", "Others", "📦"),
                    Triple("cat_inc_part_time", "Part-Time", "⏱️"),
                    Triple("cat_inc_rental", "Rental", "🏠"),
                    Triple("cat_inc_salary", "Salary", "💵"),
                    Triple("cat_inc_sales", "Sales", "🛍️"),
                    Triple("cat_inc_selling", "Selling", "🏷️")
                )

            incomeCategories.forEachIndexed { index, (id, name, icon) ->
                val color =
                    colors[(index + 5) % colors.size] // Offset to vary colors
                db.execSQL(
                    "INSERT OR IGNORE INTO categories (id, user_id, name, icon, color, type, is_default, sort_order, created_at) VALUES ('$id', 'local_user', '$name', '$icon', '$color', 'INCOME', 1, $index, 0)"
                )
            }

            // 4. Default Cash Account
            db.execSQL(
                "INSERT OR IGNORE INTO accounts (id, user_id, name, balance, currency, type, color, icon, is_default, is_active, is_excluded_from_total, sort_order, created_at, updated_at) VALUES ('acc_default_cash', 'local_user', 'Cash', 0.0, 'USD', 'CASH', '#4CAF50', '💵', 1, 1, 0, 0, 0, 0)"
            )

            // 5. Default Labels
            val labels =
                listOf(
                    Triple("lbl_coffee", "Coffee", "#795548"),
                    Triple("lbl_lunch", "Lunch", "#FF9800"),
                    Triple("lbl_must_haves", "Must Haves", "#F44336"),
                    Triple("lbl_vacation", "Vacation", "#2196F3"),
                    Triple("lbl_wants", "Wants", "#4CAF50")
                )
            labels.forEachIndexed { index, (id, name, color) ->
                db.execSQL(
                    "INSERT OR IGNORE INTO labels (id, user_id, name, color, auto_assign, sort_order, created_at, updated_at) VALUES ('$id', 'local_user', '$name', '$color', 0, $index, 0, 0)"
                )
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
