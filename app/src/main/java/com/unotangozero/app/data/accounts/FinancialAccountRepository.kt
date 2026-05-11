package com.unotangozero.app.data.accounts

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.unotangozero.app.domain.enums.FinancialAccountType
import com.unotangozero.app.domain.models.FinancialAccount
import com.unotangozero.app.domain.models.FinancialAccountsSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.financialAccountsDataStore by preferencesDataStore(name = "financial_accounts")

@Singleton
class FinancialAccountRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val accountsKey = stringPreferencesKey("accounts_json")
    private val listType = object : TypeToken<List<FinancialAccount>>() {}.type

    val accounts: Flow<List<FinancialAccount>> = context.financialAccountsDataStore.data.map { preferences ->
        val json = preferences[accountsKey].orEmpty()
        if (json.isBlank()) emptyList() else runCatching {
            gson.fromJson<List<FinancialAccount>>(json, listType)
        }.getOrDefault(emptyList())
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
            preferences[accountsKey] = gson.toJson(updated)
        }
    }

    suspend fun archive(accountId: String): Result<Unit> = runCatching {
        context.financialAccountsDataStore.edit { preferences ->
            val current = accountsFromPreferences(preferences[accountsKey])
            val updated = current.map { if (it.id == accountId) it.copy(isArchived = true) else it }
            preferences[accountsKey] = gson.toJson(updated)
        }
    }

    private fun accountsFromPreferences(json: String?): List<FinancialAccount> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching { gson.fromJson<List<FinancialAccount>>(json, listType) }.getOrDefault(emptyList())
    }
}
