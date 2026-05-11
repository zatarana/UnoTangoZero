package com.unotangozero.app.presentation.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.domain.enums.ExpenseCategory
import com.unotangozero.app.domain.models.Expense
import com.unotangozero.app.domain.repositories.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    private val currentMonth = YearMonth.now()

    val expenses: StateFlow<List<Expense>> = expenseRepository
        .observeByMonth(currentMonth)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val totalMonthInCents: StateFlow<Long> = expenses
        .combine(MutableStateFlow(Unit)) { expensesList, _ -> expensesList.sumOf { it.amountInCents } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0L
        )

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _amountText = MutableStateFlow("")
    val amountText: StateFlow<String> = _amountText.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ExpenseCategory.FOOD)
    val selectedCategory: StateFlow<ExpenseCategory> = _selectedCategory.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onDescriptionChange(value: String) {
        _description.value = value
    }

    fun onAmountChange(value: String) {
        _amountText.value = value.filter { it.isDigit() || it == ',' || it == '.' }
    }

    fun onCategoryChange(category: ExpenseCategory) {
        _selectedCategory.value = category
    }

    fun createExpense() {
        val description = _description.value.trim()
        val amountInCents = parseMoneyToCents(_amountText.value)

        if (description.isBlank()) {
            _message.value = "Digite uma descrição para o gasto."
            return
        }

        if (amountInCents <= 0L) {
            _message.value = "Digite um valor maior que zero."
            return
        }

        viewModelScope.launch {
            val expense = Expense(
                amountInCents = amountInCents,
                category = _selectedCategory.value,
                description = description,
                date = LocalDate.now()
            )

            expenseRepository.save(expense)
                .onSuccess {
                    _description.value = ""
                    _amountText.value = ""
                    _message.value = "Gasto registrado."
                }
                .onFailure {
                    _message.value = it.message ?: "Não foi possível registrar o gasto."
                }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.delete(expense.id)
                .onSuccess { _message.value = "Gasto excluído." }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir o gasto." }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue
            .trim()
            .replace(".", "")
            .replace(",", ".")

        val amount = normalized.toDoubleOrNull() ?: return 0L
        return kotlin.math.round(amount * 100).toLong()
    }
}
