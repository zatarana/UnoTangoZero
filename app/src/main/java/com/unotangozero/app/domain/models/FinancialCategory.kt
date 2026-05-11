package com.unotangozero.app.domain.models

import java.util.UUID

enum class FinancialCategoryType(val displayName: String) {
    INCOME("Receita"),
    EXPENSE("Despesa")
}

data class FinancialCategory(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: FinancialCategoryType,
    val parentName: String? = null,
    val isArchived: Boolean = false
) {
    val displayLabel: String = parentName?.let { "$it > $name" } ?: name
}
