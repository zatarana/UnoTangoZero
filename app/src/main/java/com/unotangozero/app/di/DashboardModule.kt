package com.unotangozero.app.di

import com.unotangozero.app.data.repositories.DashboardRepositoryImpl
import com.unotangozero.app.domain.repositories.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {
    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}
