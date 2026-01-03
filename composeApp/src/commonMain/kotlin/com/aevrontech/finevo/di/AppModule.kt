package com.aevrontech.finevo.di

import com.aevrontech.finevo.data.local.LocalDataSource
import com.aevrontech.finevo.data.local.createDatabase
import com.aevrontech.finevo.data.remote.AuthService
import com.aevrontech.finevo.data.repository.AccountRepositoryImpl
import com.aevrontech.finevo.data.repository.AuthRepositoryImpl
import com.aevrontech.finevo.data.repository.CategoryRepositoryImpl
import com.aevrontech.finevo.data.repository.DebtRepositoryImpl
import com.aevrontech.finevo.data.repository.ExpenseRepositoryImpl
import com.aevrontech.finevo.data.repository.HabitRepositoryImpl
import com.aevrontech.finevo.data.repository.LabelRepositoryImpl
import com.aevrontech.finevo.data.repository.SettingsRepositoryImpl
import com.aevrontech.finevo.domain.repository.AccountRepository
import com.aevrontech.finevo.domain.repository.AuthRepository
import com.aevrontech.finevo.domain.repository.CategoryRepository
import com.aevrontech.finevo.domain.repository.DebtRepository
import com.aevrontech.finevo.domain.repository.ExpenseRepository
import com.aevrontech.finevo.domain.repository.HabitRepository
import com.aevrontech.finevo.domain.repository.LabelRepository
import com.aevrontech.finevo.domain.repository.SettingsRepository
import com.aevrontech.finevo.presentation.auth.AuthViewModel
import com.aevrontech.finevo.presentation.category.CategoryViewModel
import com.aevrontech.finevo.presentation.debt.DebtViewModel
import com.aevrontech.finevo.presentation.expense.AccountViewModel
import com.aevrontech.finevo.presentation.expense.ExpenseViewModel
import com.aevrontech.finevo.presentation.habit.HabitViewModel
import com.aevrontech.finevo.presentation.home.HomeViewModel
import com.aevrontech.finevo.presentation.label.LabelViewModel
import com.aevrontech.finevo.presentation.onboarding.OnboardingViewModel
import com.aevrontech.finevo.presentation.settings.SettingsViewModel
import com.aevrontech.finevo.presentation.settings.UserProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Main application dependency injection module. */
val appModule = module {

    // Database
    single { createDatabase(get()) }
    single { LocalDataSource(get()) }

    // Services
    single { AuthService() }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<ExpenseRepository> { ExpenseRepositoryImpl(get()) }
    single<DebtRepository> { DebtRepositoryImpl(get()) }
    single<HabitRepository> { HabitRepositoryImpl(get()) }
    single<AccountRepository> { AccountRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<LabelRepository> { LabelRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl() }

    // ViewModels
    viewModel { OnboardingViewModel(get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel() }
    viewModel { ExpenseViewModel(get(), get(), get()) } // ExpenseRepo + AccountRepo + LabelRepo
    viewModel { DebtViewModel(get()) }
    viewModel { HabitViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { AccountViewModel(get()) }
    viewModel { CategoryViewModel(get()) }
    viewModel { LabelViewModel(get()) }
    viewModel { UserProfileViewModel(get()) }
}

/** Platform-specific module - override this in each platform. */
expect fun platformModule(): org.koin.core.module.Module
