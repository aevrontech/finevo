# FinEvo - Changelog

## [0.2.0-alpha] - 2024-12-25

### Phase 4: Core Features ✅

#### Add/Edit Dialogs
- **AddTransactionDialog** - Amount input, type toggle (Income/Expense), category grid, description
- **AddDebtDialog** - Name, debt type picker, balance, interest rate, minimum payment, due day
- **AddHabitDialog** - Icon picker, color selector, frequency (Daily/Weekly), XP reward

#### ViewModels Implemented
- **ExpenseViewModel** - Transaction CRUD, monthly summary, categories, dialog state
- **DebtViewModel** - Debt CRUD, payment recording, payoff strategy calculation
- **HabitViewModel** - Habit CRUD, toggle completion, streak tracking, XP rewards

#### Tab UIs (HomeScreen)
- **ExpenseTab** - Transaction list with icons, monthly income/expense summary, FAB
- **DebtTab** - Debt list with progress bars, total debt card, payment percentages
- **HabitTab** - Today's habits with checkboxes, progress bar, streak/XP display
- **SettingsTab** - Sign out button, app version info

#### Malaysian Calculators
- **EPF (KWSP)** - Employee 11%, Employer 12-13% based on salary
- **SOCSO (PERKESO)** - Category 1/2 contributions
- **EIS (SIP)** - 0.2% each for employee/employer
- **PCB (MTD)** - Progressive tax with reliefs (personal, spouse, children, EPF)
- **Zakat** - Nisab-based calculation (2.5%)
- **Salary Breakdown** - Combined calculator with net salary

#### Files Added
- `presentation/components/Dialogs.kt` - All add dialogs
- `domain/calculator/MalaysianCalculators.kt` - All MY calculators

---

## [0.1.0-alpha] - 2024-12-24

### Phase 1-3: Foundation & Authentication

#### Project Setup
- Kotlin Multiplatform (KMP) with Compose Multiplatform
- Gradle with version catalogs (`libs.versions.toml`)
- Android & iOS targets configured

#### Dependencies
- **UI:** Compose Multiplatform, Material3
- **Navigation:** Voyager
- **Database:** SQLDelight
- **Backend:** Supabase (Auth, Postgrest)
- **DI:** Koin

#### Domain Models
- User, Transaction, Category, Budget, Debt, DebtPayment, Habit, HabitLog, Bill

#### Repository Implementations
- AuthRepositoryImpl, ExpenseRepositoryImpl, DebtRepositoryImpl, HabitRepositoryImpl

#### Authentication
- Email/password sign in/up
- Google Sign-In (Android Credential Manager + nonce)
- Apple Sign-In button (iOS only)
- Session persistence

#### Database (SQLDelight)
- 12 tables with full CRUD queries
- Indexes for performance

#### Screens
- SplashScreen, OnboardingScreen, LoginScreen, HomeScreen (5-tab navigation)

---

## Next Steps

- [ ] Charts & Analytics (pie, line, bar charts)
- [ ] Transaction/Habit Calendar views
- [ ] Category management screen
- [ ] Localization (EN, MS, ZH)
- [ ] Cloud sync for premium users
