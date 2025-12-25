# FinEvo - Changelog

## Phases 1-3 Implementation Summary
**Date:** 2024-12-24  
**Version:** 0.1.0-alpha

---

## Phase 1: Project Foundation ✅

### Project Setup
- Kotlin Multiplatform (KMP) project with Compose Multiplatform
- Gradle with version catalogs (`libs.versions.toml`)
- Android & iOS targets configured

### Dependencies Integrated
- **UI:** Compose Multiplatform, Material3
- **Navigation:** Voyager
- **Database:** SQLDelight with SQLCipher support
- **Networking:** Ktor Client
- **Backend:** Supabase (Auth, Postgrest, Realtime, Storage)
- **DI:** Koin
- **DateTime:** kotlinx-datetime

### Domain Layer
- `User` - User profile with tier system
- `Transaction` - Income/Expense tracking
- `Category` - Transaction categories
- `Budget` - Budget management
- `Debt` - Debt tracking with payoff plans
- `DebtPayment` - Payment records
- `Habit` - Habit tracking with gamification
- `HabitLog` - Habit completion logs
- `Bill` - Bill reminders

### Repository Interfaces
- `AuthRepository` - Authentication operations
- `ExpenseRepository` - Transaction/Budget CRUD
- `DebtRepository` - Debt/Payment management
- `HabitRepository` - Habit tracking with XP
- `SettingsRepository` - App preferences

### UI Theme
- Futuristic dark theme
- Custom color palette with gradients
- Material3 typography
- Financial-specific colors (profit green, loss red)

### Screens Implemented
- **SplashScreen** - Animated logo with gradient
- **OnboardingScreen** - Multi-page intro flow
- **LoginScreen** - Email/password authentication
- **HomeScreen** - Tab navigation (Dashboard, Expenses, Debts, Habits, Settings)

### Database Schema (SQLDelight)
- 12 tables designed for offline-first architecture
- Full CRUD queries for all entities
- Indexes for performance

---

## Phase 2: Core Infrastructure ✅

### Database Factory
- `DatabaseFactory` (expect/actual pattern)
- Android: SQLite driver
- iOS: Native SQLite driver

### Local Data Source
- `LocalDataSource` - Wrapper for all SQLDelight queries
- Flow-based reactive data access
- Domain model mapping extensions
- Default expense/income categories

### Supabase Configuration
- `SupabaseConfig` - Client initialization
- Auth, Postgrest, Realtime, Storage plugins
- Deep linking for auth callbacks

### Repository Implementations
- `ExpenseRepositoryImpl` - SQLDelight-backed
- `HabitRepositoryImpl` - With streak calculations
- `DebtRepositoryImpl` - With payoff plan calculator

### Dependency Injection
- Platform-specific Koin modules
- Database factory injection
- Repository bindings

---

## Phase 3: Authentication ✅

### Auth Service
- `AuthService` - Supabase Auth wrapper
- Email/password sign in
- Email/password sign up
- Password reset email
- Session state observation
- User-friendly error messages

### Auth Repository
- `AuthRepositoryImpl` - Uses AuthService
- `currentUser` Flow
- `isLoggedIn` Flow

### ViewModels
- `AuthViewModel` - Sign in/up flows with validation
- `SplashViewModel` - Session check on startup

### Features
- Session persistence across app restarts
- Input validation (email format, password length)
- Error state handling with Snackbar
- Loading states
- Success messages

### Social Login (Added)
- **Google Sign-In** - Android Credential Manager with GoogleIdTokenCredential
- **Apple Sign-In** - iOS AuthenticationServices
- `SocialLoginHandler` expect/actual pattern for platform-specific implementation
- Supabase OAuth integration via IDToken provider

### Dependencies Added
- `androidx.credentials:credentials:1.3.0`
- `androidx.credentials:credentials-play-services-auth:1.3.0`
- `com.google.android.libraries.identity.googleid:googleid:1.1.1`

---

## File Structure

```
composeApp/
├── src/
│   ├── commonMain/kotlin/com/aevrontech/finevo/
│   │   ├── App.kt
│   │   ├── core/util/
│   │   │   ├── Result.kt
│   │   │   └── AppException.kt
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── DatabaseFactory.kt
│   │   │   │   └── LocalDataSource.kt
│   │   │   ├── remote/
│   │   │   │   ├── SupabaseConfig.kt
│   │   │   │   └── AuthService.kt
│   │   │   └── repository/
│   │   │       ├── AuthRepositoryImpl.kt
│   │   │       ├── ExpenseRepositoryImpl.kt
│   │   │       ├── DebtRepositoryImpl.kt
│   │   │       ├── HabitRepositoryImpl.kt
│   │   │       └── SettingsRepositoryImpl.kt
│   │   ├── di/
│   │   │   └── AppModule.kt
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── User.kt
│   │   │   │   ├── Transaction.kt
│   │   │   │   ├── Debt.kt
│   │   │   │   └── Habit.kt
│   │   │   └── repository/
│   │   │       ├── AuthRepository.kt
│   │   │       ├── ExpenseRepository.kt
│   │   │       ├── DebtRepository.kt
│   │   │       ├── HabitRepository.kt
│   │   │       └── SettingsRepository.kt
│   │   ├── presentation/
│   │   │   ├── splash/
│   │   │   ├── onboarding/
│   │   │   ├── auth/
│   │   │   ├── home/
│   │   │   ├── expense/
│   │   │   ├── debt/
│   │   │   ├── habit/
│   │   │   └── settings/
│   │   └── ui/theme/
│   │       ├── Color.kt
│   │       ├── Typography.kt
│   │       └── Theme.kt
│   ├── androidMain/kotlin/com/aevrontech/finevo/
│   │   ├── MainActivity.kt
│   │   ├── FinEvoApplication.kt
│   │   ├── data/local/DatabaseFactory.android.kt
│   │   └── di/AppModule.android.kt
│   ├── iosMain/kotlin/com/aevrontech/finevo/
│   │   ├── MainViewController.kt
│   │   ├── data/local/DatabaseFactory.ios.kt
│   │   └── di/AppModule.ios.kt
│   └── commonMain/sqldelight/
│       └── com/aevrontech/finevo/data/local/FinEvoDatabase.sq
├── build.gradle.kts
└── proguard-rules.pro
```

---

## Configuration Required

### local.properties
```properties
SUPABASE_URL=your_supabase_url
SUPABASE_ANON_KEY=your_anon_key
```

### Supabase Dashboard
1. Enable Email/Password auth provider
2. (Optional) Configure Google/Apple OAuth for social login

---

## Next Steps (Phase 4+)
- [ ] Add/Edit Transaction UI
- [ ] Debt Payoff Calculator UI
- [ ] Habit Completion with Streaks
- [ ] Malaysian Calculators (EPF, PCB, SOCSO)
- [ ] Localization (EN, MS, ZH)
- [ ] Cloud sync
- [ ] Premium features
