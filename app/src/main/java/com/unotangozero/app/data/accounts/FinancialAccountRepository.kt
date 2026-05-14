package com.unotangozero.app.data.accounts

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.unotangozero.app.domain.enums.FinancialAccountType
import com.unotangozero.app.domain.models.FinancialAccount
import com.unotangozero.app.domain.models.FinancialAccountsSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.financialAccountsDataStore by preferencesDataStore(name = "financial_accounts")

@Singleton
class FinancialAccountRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val accountsKey = stringPreferencesKey("accounts_json")

    val accounts: Flow<List<FinancialAccount>> = context.financialAccountsDataStore.data.map { preferences ->
        accountsFromPreferences(preferences[accountsKey])
    }

    val summary: Flow<FinancialAccountsSummary> = accounts.map { accountsList ->
        val active = accountsList.filter { !it.isArchived }
        val assets = active
            .filter { it.type != FinancialAccountType.CREDIT_CARD }
            .sumOf { it.initialBalanceInCents }
        val creditCardDebt = active
            .filter { it.type == FinancialAccountType.CREDIT_CARD }
            .sumOf { kotlin.math.abs(it.initialBalanceInCents) }

        FinancialAccountsSummary(
            totalAssetsInCents = assets,
            totalCreditCardDebtInCents = creditCardDebt,
            activeAccountsCount = active.size
        )
    }

    suspend fun save(account: FinancialAccount): Result<Unit> = runCatching {
        context.financialAccountsDataStore.edit { preferences ->
            val current = accountsFromPreferences(preferences[accountsKey])
            val updated = current.filterNot { it.id == account.id } + account
            preferences[accountsKey] = json.encodeToString(updated)
        }
    }

    suspend fun archive(accountId: String): Result<Unit> = runCatching {
        context.financialAccountsDataStore.edit { preferences ->
            val current = accountsFromPreferences(preferences[accountsKey])
            val updated = current.map { if (it.id == accountId) it.copy(isArchived = true) else it }
            preferences[accountsKey] = json.encodeToString(updated)
        }
    }

    private fun accountsFromPreferences(rawJson: String?): List<FinancialAccount> {
        if (rawJson.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<FinancialAccount>>(rawJson) }.getOrDefault(emptyList())
    }
}
