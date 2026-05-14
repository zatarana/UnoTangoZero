package com.unotangozero.app.domain.models

import com.unotangozero.app.domain.enums.FinancialAccountType
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class FinancialAccount(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: FinancialAccountType,
    val initialBalanceInCents: Long,
    val isArchived: Boolean = false
) {
    val isLiability: Boolean
        get() = type == FinancialAccountType.CREDIT_CARD
}

@Serializable
data class FinancialAccountsSummary(
    val totalAssetsInCents: Long = 0L,
    val totalCreditCardDebtInCents: Long = 0L,
    val netWorthInCents: Long = totalAssetsInCents - totalCreditCardDebtInCents,
    val activeAccountsCount: Int = 0
)
