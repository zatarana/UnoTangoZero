package com.unotangozero.app.di

import com.unotangozero.app.data.repositories.ReminderRepositoryImpl
import com.unotangozero.app.data.repositories.SubTaskRepositoryImpl
import com.unotangozero.app.data.repositories.TaskRepositoryImpl
import com.unotangozero.app.domain.repositories.ReminderRepository
import com.unotangozero.app.domain.repositories.SubTaskRepository
import com.unotangozero.app.domain.repositories.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TaskModule {
    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindSubTaskRepository(impl: SubTaskRepositoryImpl): SubTaskRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
}
