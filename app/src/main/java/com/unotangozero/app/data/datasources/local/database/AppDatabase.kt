package com.unotangozero.app.data.datasources.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.unotangozero.app.data.datasources.local.converters.DateTimeConverters
import com.unotangozero.app.data.datasources.local.dao.HabitDao
import com.unotangozero.app.data.datasources.local.dao.ProjectDao
import com.unotangozero.app.data.datasources.local.dao.TaskDao
import com.unotangozero.app.data.datasources.local.dao.TransactionDao
import com.unotangozero.app.data.models.entities.HabitCheckInEntity
import com.unotangozero.app.data.models.entities.HabitEntity
import com.unotangozero.app.data.models.entities.ProjectEntity
import com.unotangozero.app.data.models.entities.TaskEntity
import com.unotangozero.app.data.models.entities.TransactionEntity

/**
 * Room Database configuration for UnoTangoZero.
 */
@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        HabitCheckInEntity::class,
        TransactionEntity::class,
        ProjectEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun transactionDao(): TransactionDao
    abstract fun projectDao(): ProjectDao

    companion object {
        const val DATABASE_NAME = "unotangozero.db"
    }
}
