package com.example.goaltrack.di

import com.example.goaltrack.data.repository.AuthRepositoryImpl
import com.example.goaltrack.data.repository.GoalsRepositoryImpl
import com.example.goaltrack.data.repository.MilestonesRepositoryImpl
import com.example.goaltrack.data.repository.ProgressRepositoryImpl
import com.example.goaltrack.data.repository.SettingsRepositoryImpl
import com.example.goaltrack.data.repository.StatisticsRepositoryImpl
import com.example.goaltrack.domain.repository.AuthRepository
import com.example.goaltrack.domain.repository.GoalsRepository
import com.example.goaltrack.domain.repository.MilestonesRepository
import com.example.goaltrack.domain.repository.ProgressRepository
import com.example.goaltrack.domain.repository.SettingsRepository
import com.example.goaltrack.domain.repository.StatisticsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository interfaces to their Supabase implementations.
 * Uses @Binds for zero-overhead interface-to-impl binding.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindGoalsRepository(impl: GoalsRepositoryImpl): GoalsRepository

    @Binds @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds @Singleton
    abstract fun bindStatisticsRepository(impl: StatisticsRepositoryImpl): StatisticsRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindMilestonesRepository(impl: MilestonesRepositoryImpl): MilestonesRepository
}
