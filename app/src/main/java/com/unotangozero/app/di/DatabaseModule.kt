package com.unotangozero.app.di

import android.content.Context
import androidx.room.Room
import com.unotangozero.app.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DATABASE_NAME = "uno_tango_zero.db"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideTaskDao(database: AppDatabase) = database.taskDao()
    @Provides fun provideSubTaskDao(database: AppDatabase) = database.subTaskDao()
    @Provides fun provideReminderDao(database: AppDatabase) = database.reminderDao()
    @Provides fun provideExpenseDao(database: AppDatabase) = database.expenseDao()
    @Provides fun provideBudgetDao(database: AppDatabase) = database.budgetDao()
    @Provides fun provideDebtDao(database: AppDatabase) = database.debtDao()
    @Provides fun provideDebtPaymentDao(database: AppDatabase) = database.debtPaymentDao()
    @Provides fun provideShoppingListDao(database: AppDatabase) = database.shoppingListDao()
    @Provides fun provideShoppingItemDao(database: AppDatabase) = database.shoppingItemDao()
    @Provides fun provideHabitDao(database: AppDatabase) = database.habitDao()
    @Provides fun provideHabitLogDao(database: AppDatabase) = database.habitLogDao()
    @Provides fun provideNoteDao(database: AppDatabase) = database.noteDao()
}
