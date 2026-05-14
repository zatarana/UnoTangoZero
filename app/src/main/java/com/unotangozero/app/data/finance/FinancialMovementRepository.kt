package com.unotangozero.app.data.finance

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.unotangozero.app.data.accounts.FinancialAccountRepository
import com.unotangozero.app.domain.models.AccountBalance
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.financialMovementsDataStore by preferencesDataStore(name = "financial_movements")

@Singleton
class FinancialMovementRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    accountRepository: FinancialAccountRepository
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val movementsKey = stringPreferencesKey("movements_json")

    val movements: Flow<List<FinancialMovement>> = context.financialMovementsDataStore.data.map { preferences ->
        parse(preferences[movementsKey]).sortedByDescending { it.date }
    }

    val accountBalances: Flow<List<AccountBalance>> = combine(accountRepository.accounts, movements) { accounts, movements ->
        accounts.filter { !it.isArchived }.map { account ->
            val delta = movements.sumOf { movement ->
                when (movement.type) {
                    FinancialMovementType.INCOME -> if (movement.accountId == account.id) movement.amountInCents else 0L
                    FinancialMovementType.EXPENSE -> if (movement.accountId == account.id) -movement.amountInCents else 0L
                    FinancialMovementType.ADJUSTMENT -> if (movement.accountId == account.id) movement.amountInCents else 0L
                    FinancialMovementType.TRANSFER -> when (account.id) {
                        movement.fromAccountId -> -movement.amountInCents
                        movement.toAccountId -> movement.amountInCents
                        else -> 0L
                    }
                }
            }
            AccountBalance(account = account, currentBalanceInCents = account.initialBalanceInCents + delta)
        }
    }

    suspend fun addMovement(movement: FinancialMovement): Result<Unit> = runCatching {
        context.financialMovementsDataStore.edit { preferences ->
            val current = parse(preferences[movementsKey])
            preferences[movementsKey] = json.encodeToString(current + movement)
        }
    }

    suspend fun deleteMovement(movementId: String): Result<Unit> = runCatching {
        context.financialMovementsDataStore.edit { preferences ->
            val current = parse(preferences[movementsKey])
            preferences[movementsKey] = json.encodeToString(current.filterNot { it.id == movementId })
        }
    }

    private fun parse(rawJson: String?): List<FinancialMovement> {
        if (rawJson.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<FinancialMovement>>(rawJson) }.getOrDefault(emptyList())
    }
}
