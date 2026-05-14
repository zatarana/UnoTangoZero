package com.unotangozero.app.di

import com.unotangozero.app.data.repositories.ShoppingRepositoryImpl
import com.unotangozero.app.domain.repositories.ShoppingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShoppingModule {
    @Binds
    @Singleton
    abstract fun bindShoppingRepository(impl: ShoppingRepositoryImpl): ShoppingRepository
}
