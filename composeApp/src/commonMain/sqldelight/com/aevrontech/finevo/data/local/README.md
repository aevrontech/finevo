# SQLDelight Database Migrations

This document explains the database migration setup for the FinEvo app.

## Overview

FinEvo uses **SQLDelight Native Migrations** for versioned database schema management. This approach:

- Separates schema (tables) from queries
- Enables incremental schema changes
- Automatically handles database upgrades

## File Structure

```
composeApp/src/commonMain/sqldelight/com/aevrontech/finevo/data/local/
├── 1.sqm                    # Version 1 schema (initial, all tables)
├── 2.sqm                    # Version 2 schema (future changes)
├── FinEvoDatabase.sq        # Named queries only
└── databases/               # Generated schema output
```

## Current Schema (v1)

| Table                    | Purpose                           |
|--------------------------|-----------------------------------|
| `users`                  | User accounts                     |
| `user_preferences`       | Settings (theme, currency, etc.)  |
| `categories`             | Income/expense categories         |
| `accounts`               | Financial accounts (wallet, bank) |
| `labels`                 | Transaction labels/tags           |
| `transactions`           | Income/expense records            |
| `transaction_labels`     | Transaction ↔ Label mapping       |
| `recurring_transactions` | Recurring templates               |
| `budgets`                | Budget tracking                   |
| `debts`                  | Debt tracking                     |
| `debt_payments`          | Debt payment history              |
| `bills`                  | Bill reminders                    |
| `habits`                 | Habit tracking                    |
| `habit_logs`             | Habit completion logs             |
| `habit_categories`       | Habit categories                  |
| `user_stats`             | Gamification stats                |
| `sync_queue`             | Offline sync queue                |
| `app_config`             | App settings                      |

## How Migrations Work

### Configuration

In `build.gradle.kts`:

```kotlin
sqldelight {
    databases {
        create("FinEvoDatabase") {
            packageName.set("com.aevrontech.finevo.data.local")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)              // Validates migrations
            deriveSchemaFromMigrations.set(true)    // Schema from .sqm files
        }
    }
}
```

### Adding a New Migration

1. **Create a new `.sqm` file** with the next version number:

```sql
-- 2.sqm

-- Add new column
ALTER TABLE transactions ADD COLUMN priority INTEGER DEFAULT 0;

-- Or create new table
CREATE TABLE reminders (
    id TEXT NOT NULL PRIMARY KEY,
    user_id TEXT NOT NULL,
    title TEXT NOT NULL,
    reminder_date TEXT NOT NULL,
    is_completed INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_reminders_user ON reminders(user_id);
```

2. **Add queries** in `FinEvoDatabase.sq`:

```sql
-- Reminders Queries
selectAllReminders:
SELECT * FROM reminders WHERE user_id = ? ORDER BY reminder_date;

insertReminder:
INSERT INTO reminders(id, user_id, title, reminder_date, is_completed, created_at)
VALUES (?, ?, ?, ?, ?, ?);

deleteReminder:
DELETE FROM reminders WHERE id = ?;
```

3. **Run Gradle sync** to validate:

```bash
./gradlew generateCommonMainFinEvoDatabaseInterface
```

## Best Practices

### ✅ Do

- Use `INTEGER` for booleans (0/1)
- Use `TEXT` for dates (ISO format)
- Use `INTEGER` for timestamps (epoch millis)
- Add indexes for frequently queried columns
- Use `ON DELETE CASCADE` for foreign keys

### ❌ Don't

- Modify existing `.sqm` files after release
- Delete columns in migration (use deprecation)
- Change column types (add new column instead)

## Troubleshooting

### Build fails after migration change

```bash
# Clean and rebuild
./gradlew clean
./gradlew generateCommonMainFinEvoDatabaseInterface
```

### Schema verification error

Check that:

1. All migrations are sequential (1.sqm, 2.sqm, 3.sqm...)
2. SQL syntax is valid
3. Foreign key references exist

## Related Files

- [1.sqm](./1.sqm) - Initial schema
- [FinEvoDatabase.sq](./FinEvoDatabase.sq) - All queries
- [build.gradle.kts](../../../../build.gradle.kts) - SQLDelight config
