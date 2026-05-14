package com.unotangozero.app.di

import com.unotangozero.app.data.repositories.HabitRepositoryImpl
import com.unotangozero.app.domain.repositories.HabitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HabitModule {
    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository
}
