package com.unotangozero.app.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class FinancialAccountType(val displayName: String) {
    CHECKING("Conta corrente"),
    SAVINGS("Poupança"),
    WALLET("Carteira"),
    INVESTMENT("Investimento"),
    CREDIT_CARD("Cartão de crédito")
}
