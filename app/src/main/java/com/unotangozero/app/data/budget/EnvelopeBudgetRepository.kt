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
        val monthText = month.toString()
        val monthMovements = movements.filter { YearMonth.from(it.date) == month }
        val monthEnvelopes = envelopes.filter { it.yearMonth == monthText }
        val totalIncome = monthMovements.filter { it.type == FinancialMovementType.INCOME }.sumOf { it.amountInCents }
        val totalSpent = monthMovements.filter { it.type == FinancialMovementType.EXPENSE }.sumOf { it.amountInCents }
        val statuses = monthEnvelopes.map { envelope ->
            val spent = monthMovements
                .filter { it.type == FinancialMovementType.EXPENSE && it.category?.trim()?.lowercase() == envelope.category.trim().lowercase() }
                .sumOf { it.amountInCents }
            BudgetEnvelopeStatus(envelope = envelope, spentAmountInCents = spent)
        }
        MonthlyBudgetSummary(
            yearMonth = monthText,
            totalIncomeInCents = totalIncome,
            totalAllocatedInCents = monthEnvelopes.sumOf { it.allocatedAmountInCents },
            totalSpentInCents = totalSpent,
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

    private fun read(json: String?): List<BudgetEnvelope> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching { gson.fromJson<List<BudgetEnvelope>>(json, listType) }.getOrDefault(emptyList())
    }
}
