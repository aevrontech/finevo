package com.aevrontech.finevo.domain.model

/** Habit Categories and Sub-Categories for pre-defined habits */

/** Main categories for habit organization */
enum class HabitCategoryType(val displayName: String, val icon: String, val description: String) {
    MOST_POPULAR("Popular", "🔥", "Most popular habits"),
    HEALTH("Health", "❤️", "Health & wellness habits"),
    SPORTS("Sports", "🏃", "Sports & fitness habits"),
    LIFESTYLE("Lifestyle", "🏠", "Daily lifestyle habits"),
    QUIT("Quit", "🚫", "Habits to quit or reduce")
}

/** Pre-defined sub-categories (habit templates) with icons */
enum class HabitSubCategory(
    val displayName: String,
    val icon: String,
    val categories: List<HabitCategoryType>,
    val defaultUnit: String = "count",
    val defaultGoalValue: Int = 1
) {
    // Most Popular / Health / Sports
    WALK(
        "Walk",
        "🚶",
        listOf(
            HabitCategoryType.MOST_POPULAR,
            HabitCategoryType.HEALTH,
            HabitCategoryType.SPORTS
        ),
        "steps",
        10000
    ),
    SLEEP("Sleep", "🛌", listOf(HabitCategoryType.MOST_POPULAR, HabitCategoryType.HEALTH), "hr", 8),
    DRINK_WATER(
        "Drink Water",
        "💧",
        listOf(
            HabitCategoryType.MOST_POPULAR,
            HabitCategoryType.HEALTH,
            HabitCategoryType.LIFESTYLE
        ),
        "ml",
        2000
    ),
    MEDITATION(
        "Meditation",
        "🧘",
        listOf(
            HabitCategoryType.MOST_POPULAR,
            HabitCategoryType.HEALTH,
            HabitCategoryType.LIFESTYLE
        ),
        "min",
        10
    ),
    RUN("Run", "🏃", listOf(HabitCategoryType.MOST_POPULAR, HabitCategoryType.SPORTS), "km", 5),
    STAND(
        "Stand",
        "🧍",
        listOf(
            HabitCategoryType.MOST_POPULAR,
            HabitCategoryType.HEALTH,
            HabitCategoryType.SPORTS
        ),
        "count",
        12
    ),
    CYCLING(
        "Cycling",
        "🚴",
        listOf(
            HabitCategoryType.MOST_POPULAR,
            HabitCategoryType.HEALTH,
            HabitCategoryType.SPORTS
        ),
        "km",
        10
    ),
    WORKOUT(
        "Workout",
        "💪",
        listOf(
            HabitCategoryType.MOST_POPULAR,
            HabitCategoryType.HEALTH,
            HabitCategoryType.SPORTS
        ),
        "min",
        30
    ),
    BURN_CALORIE(
        "Burn Calorie",
        "🔥",
        listOf(
            HabitCategoryType.MOST_POPULAR,
            HabitCategoryType.HEALTH,
            HabitCategoryType.SPORTS
        ),
        "Cal",
        500
    ),
    EXERCISE(
        "Exercise",
        "🏋️",
        listOf(
            HabitCategoryType.MOST_POPULAR,
            HabitCategoryType.HEALTH,
            HabitCategoryType.SPORTS
        ),
        "min",
        30
    ),
    READ_BOOK(
        "Read a Book",
        "📚",
        listOf(HabitCategoryType.MOST_POPULAR, HabitCategoryType.LIFESTYLE),
        "min",
        30
    ),
    DRINK_LESS_ALCOHOL(
        "Drink Less Alcohol",
        "🍺",
        listOf(HabitCategoryType.MOST_POPULAR, HabitCategoryType.QUIT),
        "drink",
        0
    ),
    DRINK_LESS_CAFFEINE(
        "Drink Less Caffeine",
        "☕",
        listOf(
            HabitCategoryType.MOST_POPULAR,
            HabitCategoryType.HEALTH,
            HabitCategoryType.QUIT
        ),
        "drink",
        2
    ),

    // Health specific
    LESS_CARBOHYDRATE(
        "Less Carbohydrate",
        "🍞",
        listOf(HabitCategoryType.HEALTH, HabitCategoryType.QUIT),
        "g",
        100
    ),

    // Sports specific
    STRETCH("Stretch", "🤸", listOf(HabitCategoryType.SPORTS), "min", 10),
    YOGA("Yoga", "🧘‍♀️", listOf(HabitCategoryType.SPORTS), "min", 20),
    SWIM("Swim", "🏊", listOf(HabitCategoryType.SPORTS), "min", 30),

    // Lifestyle specific
    TRACK_EXPENSES("Track Expenses", "💰", listOf(HabitCategoryType.LIFESTYLE), "count", 1),
    SAVE_MONEY("Save Money", "🐷", listOf(HabitCategoryType.LIFESTYLE), "count", 1),
    EAT_LESS_SUGAR(
        "Eat Less Sugar",
        "🍬",
        listOf(HabitCategoryType.LIFESTYLE, HabitCategoryType.QUIT),
        "count",
        1
    ),
    BREATH("Breath", "🌬️", listOf(HabitCategoryType.LIFESTYLE), "min", 5),
    LEARNING("Learning", "🎓", listOf(HabitCategoryType.LIFESTYLE), "min", 30),
    REVIEW_TODAY("Review Today", "📝", listOf(HabitCategoryType.LIFESTYLE), "count", 1),
    MIND_CLEARING("Mind Clearing", "🧠", listOf(HabitCategoryType.LIFESTYLE), "min", 10),
    EAT_FRUITS("Eat Fruits", "🍎", listOf(HabitCategoryType.LIFESTYLE), "count", 2),
    EAT_VEGE("Eat Vege", "🥗", listOf(HabitCategoryType.LIFESTYLE), "count", 2),
    NO_SUGAR("No Sugar", "🚫", listOf(HabitCategoryType.LIFESTYLE), "count", 1),
    SLEEP_EARLY("Sleep Early", "🌙", listOf(HabitCategoryType.LIFESTYLE), "count", 1),
    LAUGH_OUT_LOUD("Laugh Out Loud", "😂", listOf(HabitCategoryType.LIFESTYLE), "count", 1),
    EAT_LOW_FAT("Eat Low-Fat", "🥦", listOf(HabitCategoryType.LIFESTYLE), "count", 1),
    EAT_BREAKFAST("Eat Breakfast", "🍳", listOf(HabitCategoryType.LIFESTYLE), "count", 1),

    // Quit specific
    DRINK_LESS_BEVERAGE("Drink Less Beverage", "🥤", listOf(HabitCategoryType.QUIT), "drink", 1),
    SMOKE_LESS("Smoke Less", "🚬", listOf(HabitCategoryType.QUIT), "count", 0),
    PLAY_LESS_GAME("Play Less Game", "🎮", listOf(HabitCategoryType.QUIT), "hr", 1),
    SIT_LESS("Sit Less", "🪑", listOf(HabitCategoryType.QUIT), "hr", 6),
    COMPLAIN_LESS("Complain Less", "😤", listOf(HabitCategoryType.QUIT), "count", 0),
    WATCH_LESS_TV("Watch Less TV", "📺", listOf(HabitCategoryType.QUIT), "hr", 1),
    LESS_SOCIAL_APP("Less Social App", "📱", listOf(HabitCategoryType.QUIT), "hr", 1),
    SPEND_LESS("Spend Less", "💸", listOf(HabitCategoryType.QUIT), "count", 1);

    companion object {
        /** Get sub-categories for a specific category */
        fun getByCategory(category: HabitCategoryType): List<HabitSubCategory> {
            return entries.filter { category in it.categories }
        }

        /** Search sub-categories by name */
        fun search(query: String): List<HabitSubCategory> {
            if (query.isBlank()) return entries.toList()
            return entries.filter { it.displayName.contains(query, ignoreCase = true) }
        }
    }
}

/** Unit type classification */
enum class GoalUnitType {
    QUANTITY,
    TIME
}

/** Pre-defined goal units */
enum class GoalUnit(val displayName: String, val symbol: String, val type: GoalUnitType) {
    // Quantity units
    COUNT("Count", "count", GoalUnitType.QUANTITY),
    STEPS("Steps", "steps", GoalUnitType.QUANTITY),
    METERS("Meters", "m", GoalUnitType.QUANTITY),
    KILOMETERS("Kilometers", "km", GoalUnitType.QUANTITY),
    MILES("Miles", "mile", GoalUnitType.QUANTITY),
    MILLILITERS("Milliliters", "ml", GoalUnitType.QUANTITY),
    OUNCES("Ounces", "oz", GoalUnitType.QUANTITY),
    CALORIES("Calories", "Cal", GoalUnitType.QUANTITY),
    GRAMS("Grams", "g", GoalUnitType.QUANTITY),
    MILLIGRAMS("Milligrams", "mg", GoalUnitType.QUANTITY),
    DRINKS("Drinks", "drink", GoalUnitType.QUANTITY),

    // Time units
    SECONDS("Seconds", "sec", GoalUnitType.TIME),
    MINUTES("Minutes", "min", GoalUnitType.TIME),
    HOURS("Hours", "hr", GoalUnitType.TIME);

    companion object {
        fun getByType(type: GoalUnitType): List<GoalUnit> {
            return entries.filter { it.type == type }
        }

        fun fromSymbol(symbol: String): GoalUnit? {
            return entries.find { it.symbol == symbol }
        }
    }
}

/** Goal period options */
enum class GoalPeriod(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

/** Habit gesture mode - how user evaluates progress */
enum class HabitGestureMode(val displayName: String, val description: String) {
    MARK_AS_DONE("Mark as Done", "Simply mark habit as complete"),
    INPUT_VALUE("Input Value", "Enter a value to track progress")
}

/** Time range for habit scheduling */
enum class HabitTimeRange(val displayName: String) {
    ANYTIME("Anytime"),
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening")
}
