package com.unotangozero.app.di

import com.unotangozero.app.core.coroutines.CoroutineDispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {
    @Provides
    @Singleton
    fun provideCoroutineDispatcherProvider(): CoroutineDispatcherProvider {
        return CoroutineDispatcherProvider(
            io = Dispatchers.IO,
            default = Dispatchers.Default,
            main = Dispatchers.Main
        )
    }
}
