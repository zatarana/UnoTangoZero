package com.unotangozero.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unotangozero.app.data.local.entity.DebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debt: DebtEntity)

    @Update
    suspend fun update(debt: DebtEntity)

    @Delete
    suspend fun delete(debt: DebtEntity)

    @Query("SELECT * FROM debts ORDER BY dueDateMillis ASC")
    fun observeAll(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE status = :status ORDER BY dueDateMillis ASC")
    fun observeByStatus(status: String): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE dueDateMillis BETWEEN :startMillis AND :endMillis ORDER BY dueDateMillis ASC")
    fun observeDueBetween(startMillis: Long, endMillis: Long): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE creditor LIKE '%' || :query || '%' ORDER BY dueDateMillis ASC")
    fun searchByCreditor(query: String): Flow<List<DebtEntity>>

    @Query("UPDATE debts SET remainingAmountInCents = :remainingAmountInCents, status = :status, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updatePaymentState(id: String, remainingAmountInCents: Long, status: String, updatedAtMillis: Long)

    @Query("SELECT COALESCE(SUM(remainingAmountInCents), 0) FROM debts WHERE status != 'PAID'")
    fun observeOpenAmountInCents(): Flow<Long>
}
