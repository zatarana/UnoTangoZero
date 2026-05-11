package com.unotangozero.app.data.budget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.unotangozero.app.data.finance.FinancialMovementRepository
import com.unotangozero.app.domain.models.BudgetEnvelope
import com.unotangozero.app.domain.models.BudgetEnvelopeStatus
import com.unotangozero.app.domain.models.FinancialMovementType
import com.unotangozero.app.domain.models.MonthlyBudgetSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

private val Context.envelopeBudgetDataStore by preferencesDataStore(name = "envelope_budget")

@Singleton
class EnvelopeBudgetRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    movementRepository: FinancialMovementRepository
) {
    private val gson = Gson()
    private val envelopesKey = stringPreferencesKey("envelopes_json")
    private val listType = object : TypeToken<List<BudgetEnvelope>>() {}.type

    val envelopes: Flow<List<BudgetEnvelope>> = context.envelopeBudgetDataStore.data.map { preferences ->
        read(preferences[envelopesKey]).sortedBy { it.category }
    }

    val currentMonthSummary: Flow<MonthlyBudgetSummary> = combine(envelopes, movementRepository.movements) { envelopes, movements ->
        val month = YearMonth.now()
        val previousMonth = month.minusMonths(1)
        val monthText = month.toString()
        val previousMonthText = previousMonth.toString()

        val monthMovements = movements.filter { YearMonth.from(it.date) == month }
        val previousMonthMovements = movements.filter { YearMonth.from(it.date) == previousMonth }
        val monthEnvelopes = envelopes.filter { it.yearMonth == monthText }
        val previousMonthEnvelopes = envelopes.filter { it.yearMonth == previousMonthText && it.rolloverEnabled }

        val totalIncome = monthMovements.filter { it.type == FinancialMovementType.INCOME }.sumOf { it.amountInCents }
        val totalSpent = monthMovements.filter { it.type == FinancialMovementType.EXPENSE }.sumOf { it.amountInCents }

        val rolloverByCategory = previousMonthEnvelopes.associate { previousEnvelope ->
            val previousSpent = spentByCategory(previousMonthMovements, previousEnvelope.category)
            val previousRemaining = previousEnvelope.allocatedAmountInCents - previousSpent
            previousEnvelope.category.trim().lowercase() to previousRemaining.coerceAtLeast(0L)
        }

        val statuses = monthEnvelopes.map { envelope ->
            val spent = spentByCategory(monthMovements, envelope.category)
            val rollover = rolloverByCategory[envelope.category.trim().lowercase()] ?: 0L
            BudgetEnvelopeStatus(envelope = envelope, spentAmountInCents = spent, rolloverAmountInCents = rollover)
        }

        MonthlyBudgetSummary(
            yearMonth = monthText,
            totalIncomeInCents = totalIncome,
            totalAllocatedInCents = monthEnvelopes.sumOf { it.allocatedAmountInCents },
            totalSpentInCents = totalSpent,
            totalRolloverInCents = statuses.sumOf { it.rolloverAmountInCents },
            envelopes = statuses
        )
    }

    suspend fun saveEnvelope(envelope: BudgetEnvelope): Result<Unit> = runCatching {
        context.envelopeBudgetDataStore.edit { preferences ->
            val current = read(preferences[envelopesKey])
            val updated = current.filterNot { it.id == envelope.id } + envelope
            preferences[envelopesKey] = gson.toJson(updated)
        }
    }

    suspend fun deleteEnvelope(envelopeId: String): Result<Unit> = runCatching {
        context.envelopeBudgetDataStore.edit { preferences ->
            preferences[envelopesKey] = gson.toJson(read(preferences[envelopesKey]).filterNot { it.id == envelopeId })
        }
    }

    private fun spentByCategory(movements: List<com.unotangozero.app.domain.models.FinancialMovement>, category: String): Long {
        return movements
            .filter { it.type == FinancialMovementType.EXPENSE && it.category?.trim()?.lowercase() == category.trim().lowercase() }
            .sumOf { it.amountInCents }
    }

    private fun read(json: String?): List<BudgetEnvelope> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching { gson.fromJson<List<BudgetEnvelope>>(json, listType) }.getOrDefault(emptyList())
    }
}
