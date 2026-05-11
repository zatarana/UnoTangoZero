package com.unotangozero.app.data.finance

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.unotangozero.app.data.accounts.FinancialAccountRepository
import com.unotangozero.app.domain.enums.FinancialAccountType
import com.unotangozero.app.domain.models.AccountBalance
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.financialMovementsDataStore by preferencesDataStore(name = "financial_movements")

@Singleton
class FinancialMovementRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    accountRepository: FinancialAccountRepository
) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ -> JsonPrimitive(src.toString()) })
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ -> LocalDate.parse(json.asString) })
        .registerTypeAdapter(LocalDateTime::class.java, JsonSerializer<LocalDateTime> { src, _, _ -> JsonPrimitive(src.toString()) })
        .registerTypeAdapter(LocalDateTime::class.java, JsonDeserializer { json, _, _ -> LocalDateTime.parse(json.asString) })
        .create()

    private val movementsKey = stringPreferencesKey("movements_json")
    private val listType = object : TypeToken<List<FinancialMovement>>() {}.type

    val movements: Flow<List<FinancialMovement>> = context.financialMovementsDataStore.data.map { preferences ->
        parse(preferences[movementsKey]).sortedByDescending { it.date }
    }

    val accountBalances: Flow<List<AccountBalance>> = combine(accountRepository.accounts, movements) { accounts, movements ->
        accounts.filter { !it.isArchived }.map { account ->
            val delta = movements.sumOf { movement ->
                when (movement.type) {
                    FinancialMovementType.INCOME -> if (movement.accountId == account.id) movement.amountInCents else 0L
                    FinancialMovementType.TRANSFER -> when (account.id) {
                        movement.fromAccountId -> -movement.amountInCents
                        movement.toAccountId -> movement.amountInCents
                        else -> 0L
                    }
                }
            }
            val balance = if (account.type == FinancialAccountType.CREDIT_CARD) {
                account.initialBalanceInCents + delta
            } else {
                account.initialBalanceInCents + delta
            }
            AccountBalance(account = account, currentBalanceInCents = balance)
        }
    }

    suspend fun addMovement(movement: FinancialMovement): Result<Unit> = runCatching {
        context.financialMovementsDataStore.edit { preferences ->
            val current = parse(preferences[movementsKey])
            preferences[movementsKey] = gson.toJson(current + movement)
        }
    }

    suspend fun deleteMovement(movementId: String): Result<Unit> = runCatching {
        context.financialMovementsDataStore.edit { preferences ->
            val current = parse(preferences[movementsKey])
            preferences[movementsKey] = gson.toJson(current.filterNot { it.id == movementId })
        }
    }

    private fun parse(json: String?): List<FinancialMovement> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching { gson.fromJson<List<FinancialMovement>>(json, listType) }.getOrDefault(emptyList())
    }
}
