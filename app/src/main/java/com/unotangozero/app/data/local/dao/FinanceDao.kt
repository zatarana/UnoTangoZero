package com.unotangozero.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unotangozero.app.data.local.entity.BillEntity
import com.unotangozero.app.data.local.entity.BudgetEntity
import com.unotangozero.app.data.local.entity.CategoryEntity
import com.unotangozero.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: BillEntity)

    @Update
    suspend fun update(bill: BillEntity)

    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("SELECT * FROM bills ORDER BY dueDateMillis ASC")
    fun observeAll(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE status = :status ORDER BY dueDateMillis ASC")
    fun observeByStatus(status: String): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE dueDateMillis BETWEEN :startMillis AND :endMillis ORDER BY dueDateMillis ASC")
    fun observeDueBetween(startMillis: Long, endMillis: Long): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE categoryId = :categoryId ORDER BY dueDateMillis DESC")
    fun observeByCategory(categoryId: String): Flow<List<BillEntity>>
}

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets ORDER BY monthYear DESC")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear ORDER BY categoryId ASC")
    fun observeByMonth(monthYear: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId ORDER BY monthYear DESC")
    fun observeByCategory(categoryId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND monthYear = :monthYear LIMIT 1")
    suspend fun getForCategoryAndMonth(categoryId: String, monthYear: String): BudgetEntity?
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type AND isArchived = 0 ORDER BY name ASC")
    fun observeByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isArchived = 0 ORDER BY name ASC")
    fun observeActive(): Flow<List<CategoryEntity>>

    @Query("UPDATE categories SET isArchived = 1, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun archive(id: String, updatedAtMillis: Long)
}

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateMillis DESC")
    fun observeByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY dateMillis DESC")
    fun observeByCategory(categoryId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR fromAccountId = :accountId OR toAccountId = :accountId ORDER BY dateMillis DESC")
    fun observeByAccount(accountId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE installmentGroupId = :groupId ORDER BY installmentNumber ASC")
    fun observeInstallmentGroup(groupId: String): Flow<List<TransactionEntity>>
}
