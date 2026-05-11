package com.unotangozero.app.presentation.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.domain.enums.ExpenseCategory
import com.unotangozero.app.domain.models.Budget
import com.unotangozero.app.domain.models.BudgetStatus
import com.unotangozero.app.domain.models.Expense
import com.unotangozero.app.domain.repositories.BudgetRepository
import com.unotangozero.app.domain.repositories.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class FinanceReportUiState(
    val totalInCents: Long = 0L,
    val expenseCount: Int = 0,
    val averageExpenseInCents: Long = 0L,
    val topCategory: ExpenseCategory? = null,
    val categoryTotals: List<CategoryTotalUiState> = emptyList()
)

data class CategoryTotalUiState(
    val category: ExpenseCategory,
    val totalInCents: Long,
    val percentage: Double
)

data class ExpenseEditorUiState(
    val editingExpense: Expense? = null,
    val description: String = "",
    val amountText: String = "",
    val category: ExpenseCategory = ExpenseCategory.FOOD,
    val date: LocalDate = LocalDate.now()
) {
    val isEditing: Boolean = editingExpense != null
}

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {
    private val currentMonth = YearMonth.now()

    val expenses: StateFlow<List<Expense>> = expenseRepository
        .observeByMonth(currentMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val budgetStatus: StateFlow<List<BudgetStatus>> = budgetRepository
        .observeBudgetStatus(currentMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalMonthInCents: StateFlow<Long> = expenses
        .map { it.sumOf { expense -> expense.amountInCents } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val report: StateFlow<FinanceReportUiState> = expenses
        .map { expensesList ->
            val total = expensesList.sumOf { it.amountInCents }
            val categoryTotals = expensesList
                .groupBy { it.category }
                .map { (category, items) ->
                    val categoryTotal = items.sumOf { it.amountInCents }
                    CategoryTotalUiState(
                        category = category,
                        totalInCents = categoryTotal,
                        percentage = if (total > 0L) categoryTotal.toDouble() / total.toDouble() * 100.0 else 0.0
                    )
                }
                .sortedByDescending { it.totalInCents }

            FinanceReportUiState(
                totalInCents = total,
                expenseCount = expensesList.size,
                averageExpenseInCents = if (expensesList.isNotEmpty()) total / expensesList.size else 0L,
                topCategory = categoryTotals.firstOrNull()?.category,
                categoryTotals = categoryTotals
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceReportUiState())

    private val _expenseEditorState = MutableStateFlow(ExpenseEditorUiState())
    val expenseEditorState: StateFlow<ExpenseEditorUiState> = _expenseEditorState.asStateFlow()

    private val _budgetLimitText = MutableStateFlow("")
    val budgetLimitText: StateFlow<String> = _budgetLimitText.asStateFlow()

    private val _selectedBudgetCategory = MutableStateFlow(ExpenseCategory.FOOD)
    val selectedBudgetCategory: StateFlow<ExpenseCategory> = _selectedBudgetCategory.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onExpenseDescriptionChange(value: String) {
        _expenseEditorState.value = _expenseEditorState.value.copy(description = value)
    }

    fun onExpenseAmountChange(value: String) {
        _expenseEditorState.value = _expenseEditorState.value.copy(amountText = value.filterMoneyChars())
    }

    fun onExpenseCategoryChange(category: ExpenseCategory) {
        _expenseEditorState.value = _expenseEditorState.value.copy(category = category)
    }

    fun onExpenseDatePreviousDay() {
        _expenseEditorState.value = _expenseEditorState.value.copy(date = _expenseEditorState.value.date.minusDays(1))
    }

    fun onExpenseDateNextDay() {
        _expenseEditorState.value = _expenseEditorState.value.copy(date = _expenseEditorState.value.date.plusDays(1))
    }

    fun startEditingExpense(expense: Expense) {
        _expenseEditorState.value = ExpenseEditorUiState(
            editingExpense = expense,
            description = expense.description,
            amountText = centsToMoneyText(expense.amountInCents),
            category = expense.category,
            date = expense.date
        )
    }

    fun cancelExpenseEditing() {
        _expenseEditorState.value = ExpenseEditorUiState()
    }

    fun onBudgetLimitChange(value: String) { _budgetLimitText.value = value.filterMoneyChars() }
    fun onBudgetCategoryChange(category: ExpenseCategory) { _selectedBudgetCategory.value = category }

    fun saveExpenseFromEditor() {
        val state = _expenseEditorState.value
        val description = state.description.trim()
        val amountInCents = parseMoneyToCents(state.amountText)

        if (description.isBlank()) {
            _message.value = "Digite uma descrição para o gasto."
            return
        }
        if (amountInCents <= 0L) {
            _message.value = "Digite um valor maior que zero."
            return
        }

        viewModelScope.launch {
            val expense = state.editingExpense?.copy(
                amountInCents = amountInCents,
                category = state.category,
                description = description,
                date = state.date
            ) ?: Expense(
                amountInCents = amountInCents,
                category = state.category,
                description = description,
                date = state.date
            )

            expenseRepository.save(expense)
                .onSuccess {
                    _expenseEditorState.value = ExpenseEditorUiState()
                    _message.value = if (state.isEditing) "Gasto atualizado." else "Gasto registrado."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível salvar o gasto." }
        }
    }

    fun createBudget() {
        val limitInCents = parseMoneyToCents(_budgetLimitText.value)
        if (limitInCents <= 0L) {
            _message.value = "Digite um limite maior que zero."
            return
        }

        viewModelScope.launch {
            val budget = Budget(
                yearMonth = currentMonth.toString(),
                category = _selectedBudgetCategory.value,
                limitAmountInCents = limitInCents
            )
            budgetRepository.save(budget)
                .onSuccess {
                    _budgetLimitText.value = ""
                    _message.value = "Orçamento salvo."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível salvar o orçamento." }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.delete(expense.id)
                .onSuccess {
                    if (_expenseEditorState.value.editingExpense?.id == expense.id) cancelExpenseEditing()
                    _message.value = "Gasto excluído."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir o gasto." }
        }
    }

    fun clearMessage() { _message.value = null }

    private fun String.filterMoneyChars(): String = filter { it.isDigit() || it == ',' || it == '.' }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue.trim().replace(".", "").replace(",", ".")
        val amount = normalized.toDoubleOrNull() ?: return 0L
        return kotlin.math.round(amount * 100).toLong()
    }

    private fun centsToMoneyText(amountInCents: Long): String {
        return "%.2f".format(amountInCents / 100.0).replace('.', ',')
    }
}
