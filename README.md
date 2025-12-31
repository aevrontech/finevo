# FinEvo

A Kotlin Multiplatform (KMP) finance and habit tracking application with offline-first architecture.

## Overview

**FinEvo** is a comprehensive personal finance and productivity app targeting both iOS and Android platforms using Kotlin Multiplatform with shared UI via Compose Multiplatform.

### Key Features
- 💰 **Expense Tracker** - Track income/expenses with categories and budgets
- 🎯 **Debt Payoff Planner** - Avalanche/Snowball strategies with visualizations
- 🚀 **Habit Tracker** - Daily habits with gamification (XP, levels, streaks)
- 🇲🇾 **Malaysian Tax Calculators** - EPF, PCB, SOCSO, Zakat (for Malaysian users)

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Kotlin Multiplatform | Shared business logic |
| Compose Multiplatform | Shared UI |
| SQLDelight | Local database |
| Supabase | Backend (Auth, Database, Storage) |
| Koin | Dependency injection |
| Voyager | Navigation |
| Ktor | Networking |

## Project Structure

```
finevo/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/           # Shared code (95%)
│   │   │   ├── kotlin/com/aevrontech/finevo/
│   │   │   │   ├── core/         # Utilities, base classes
│   │   │   │   ├── data/         # Repositories, data sources
│   │   │   │   ├── di/           # Dependency injection
│   │   │   │   ├── domain/       # Models, repository interfaces
│   │   │   │   ├── presentation/ # ViewModels, screens
│   │   │   │   └── ui/           # Theme, components
│   │   │   └── sqldelight/       # Database schema
│   │   ├── androidMain/          # Android-specific
│   │   └── iosMain/              # iOS-specific
├── gradle/
│   └── libs.versions.toml        # Dependency versions
└── local.properties.example      # Secrets template
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Xcode 15+ (for iOS builds)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/aevrontech/finevo.git
   cd finevo
   ```

2. **Configure secrets**
   ```bash
   cp local.properties.example local.properties
   ```
   
   Edit `local.properties` and add your Supabase credentials:
   ```properties
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   ```

3. **Open in Android Studio**
   - File → Open → Select the project folder
   - Wait for Gradle sync to complete

4. **Run the app**
   - Select `composeApp` configuration
   - Click Run ▶

### iOS Setup (macOS only)

1. Open the `iosApp/iosApp.xcodeproj` in Xcode
2. Select your simulator/device
3. Build and run

## Development

### Build Commands

```bash
# Build all targets
./gradlew build

# Android debug build
./gradlew :composeApp:assembleDebug

# Run tests
./gradlew :composeApp:allTests

# Generate SQLDelight code
./gradlew :composeApp:generateCommonMainFinEvoDatabaseInterface
```

### Architecture

The project follows **Clean Architecture** with **MVI** pattern:

```
presentation (UI) → domain (Business Logic) → data (Repositories)
```

- **Domain Layer**: Contains entities, repository interfaces, and use cases
- **Data Layer**: Implements repositories, handles local DB and remote API
- **Presentation Layer**: ViewModels manage UI state, Compose handles rendering

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## License

Copyright © 2026 Aevrontech. All rights reserved.

## Contact

- **Company**: Aevrontech
- **Package**: com.aevrontech.finevo
