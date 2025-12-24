package com.aevrontech.finevo.di

import com.aevrontech.finevo.data.local.DatabaseFactory
import com.aevrontech.finevo.data.local.LocalDataSource
import com.aevrontech.finevo.data.local.createDatabase
import com.aevrontech.finevo.data.repository.*
import com.aevrontech.finevo.domain.repository.*
import com.aevrontech.finevo.presentation.auth.AuthViewModel
import com.aevrontech.finevo.presentation.debt.DebtViewModel
import com.aevrontech.finevo.presentation.expense.ExpenseViewModel
import com.aevrontech.finevo.presentation.habit.HabitViewModel
import com.aevrontech.finevo.presentation.home.HomeViewModel
import com.aevrontech.finevo.presentation.onboarding.OnboardingViewModel
import com.aevrontech.finevo.presentation.settings.SettingsViewModel
import com.aevrontech.finevo.presentation.splash.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Main application dependency injection module.
 */
val appModule = module {
    
    // Database
    single { createDatabase(get()) }
    single { LocalDataSource(get()) }
    
    // Repositories
    single<AuthRepository> { AuthRepositoryImpl() }
    single<ExpenseRepository> { ExpenseRepositoryImpl(get()) }
    single<DebtRepository> { DebtRepositoryImpl(get()) }
    single<HabitRepository> { HabitRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl() }
    
    // ViewModels
    viewModel { SplashViewModel() }
    viewModel { OnboardingViewModel() }
    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel() }
    viewModel { ExpenseViewModel(get()) }
    viewModel { DebtViewModel(get()) }
    viewModel { HabitViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}

/**
 * Platform-specific module - override this in each platform.
 */
expect fun platformModule(): org.koin.core.module.Module
