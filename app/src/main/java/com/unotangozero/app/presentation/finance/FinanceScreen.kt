package com.unotangozero.app.presentation.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.enums.ExpenseCategory
import com.unotangozero.app.domain.models.BudgetStatus
import com.unotangozero.app.domain.models.Expense
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FinanceRoute(viewModel: FinanceViewModel = hiltViewModel()) {
    val expenses by viewModel.expenses.collectAsState()
    val budgetStatus by viewModel.budgetStatus.collectAsState()
    val report by viewModel.report.collectAsState()
    val totalMonthInCents by viewModel.totalMonthInCents.collectAsState()
    val description by viewModel.description.collectAsState()
    val amountText by viewModel.amountText.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val budgetLimitText by viewModel.budgetLimitText.collectAsState()
    val selectedBudgetCategory by viewModel.selectedBudgetCategory.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val currentMessage = message
        if (currentMessage != null) {
            snackbarHostState.showSnackbar(currentMessage)
            viewModel.clearMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)
        FinanceScreen(
            expenses = expenses,
            budgetStatus = budgetStatus,
            report = report,
            totalMonthInCents = totalMonthInCents,
            description = description,
            amountText = amountText,
            selectedCategory = selectedCategory,
            budgetLimitText = budgetLimitText,
            selectedBudgetCategory = selectedBudgetCategory,
            onDescriptionChange = viewModel::onDescriptionChange,
            onAmountChange = viewModel::onAmountChange,
            onCategoryChange = viewModel::onCategoryChange,
            onBudgetLimitChange = viewModel::onBudgetLimitChange,
            onBudgetCategoryChange = viewModel::onBudgetCategoryChange,
            onCreateExpense = viewModel::createExpense,
            onCreateBudget = viewModel::createBudget,
            onDeleteExpense = viewModel::deleteExpense
        )
    }
}

@Composable
fun FinanceScreen(
    expenses: List<Expense>,
    budgetStatus: List<BudgetStatus>,
    report: FinanceReportUiState,
    totalMonthInCents: Long,
    description: String,
    amountText: String,
    selectedCategory: ExpenseCategory,
    budgetLimitText: String,
    selectedBudgetCategory: ExpenseCategory,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (ExpenseCategory) -> Unit,
    onBudgetLimitChange: (String) -> Unit,
    onBudgetCategoryChange: (ExpenseCategory) -> Unit,
    onCreateExpense: () -> Unit,
    onCreateBudget: () -> Unit,
    onDeleteExpense: (Expense) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Finanças", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Registre gastos, defina orçamentos e acompanhe seu relatório mensal.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Total gasto no mês", style = MaterialTheme.typography.labelLarge)
                    Text(formatMoney(totalMonthInCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("${report.expenseCount} gasto(s) • média ${formatMoney(report.averageExpenseInCents)}", style = MaterialTheme.typography.bodyMedium)
                    report.topCategory?.let { Text("Categoria principal: ${it.displayName}", style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        if (report.categoryTotals.isNotEmpty()) {
            item { SectionTitle("Relatório por categoria") }
            items(report.categoryTotals, key = { it.category.name }) { item ->
                CategoryReportCard(item)
            }
        }

        item { SectionTitle("Novo gasto") }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = description, onValueChange = onDescriptionChange, label = { Text("Descrição") }, singleLine = true)
                    OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = amountText, onValueChange = onAmountChange, label = { Text("Valor") }, singleLine = true, prefix = { Text("R$ ") })
                    CategoryChips(selectedCategory = selectedCategory, onCategoryChange = onCategoryChange)
                    Button(modifier = Modifier.fillMaxWidth(), onClick = onCreateExpense) { Text("Registrar gasto") }
                }
            }
        }

        item { SectionTitle("Orçamento mensal") }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = budgetLimitText, onValueChange = onBudgetLimitChange, label = { Text("Limite da categoria") }, singleLine = true, prefix = { Text("R$ ") })
                    CategoryChips(selectedCategory = selectedBudgetCategory, onCategoryChange = onBudgetCategoryChange)
                    Button(modifier = Modifier.fillMaxWidth(), onClick = onCreateBudget) { Text("Salvar orçamento") }
                }
            }
        }

        if (budgetStatus.isNotEmpty()) {
            item { SectionTitle("Status dos orçamentos") }
            items(items = budgetStatus, key = { it.category.name }) { status ->
                BudgetStatusCard(status = status)
            }
        }

        item { SectionTitle("Gastos do mês") }

        if (expenses.isEmpty()) {
            item { EmptyExpensesCard() }
        } else {
            items(items = expenses, key = { it.id }) { expense ->
                ExpenseCard(expense = expense, onDeleteExpense = onDeleteExpense)
            }
        }
    }
}

@Composable
private fun CategoryReportCard(item: CategoryTotalUiState) {
    val progress = (item.percentage / 100.0).coerceIn(0.0, 1.0).toFloat()
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.category.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(formatMoney(item.totalInCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
            Text("${item.percentage.toInt()}% dos gastos do mês", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

@Composable
private fun CategoryChips(selectedCategory: ExpenseCategory, onCategoryChange: (ExpenseCategory) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ExpenseCategory.entries) { category ->
            FilterChip(selected = selectedCategory == category, onClick = { onCategoryChange(category) }, label = { Text(category.displayName) })
        }
    }
}

@Composable
private fun BudgetStatusCard(status: BudgetStatus) {
    val progress = (status.percentageUsed / 100.0).coerceIn(0.0, 1.0).toFloat()
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(status.category.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${status.percentageUsed.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
            Text("${formatMoney(status.spentAmountInCents)} de ${formatMoney(status.limitAmountInCents)}", style = MaterialTheme.typography.bodyMedium)
            val label = when {
                status.isOverBudget -> "Limite ultrapassado"
                status.isWarning -> "Atenção: acima de 80%"
                else -> "Dentro do orçamento"
            }
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyExpensesCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nenhum gasto registrado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Registre um gasto acima para acompanhar seu mês.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense, onDeleteExpense: (Expense) -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(expense.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(expense.category.displayName) })
                    Text(text = expense.date.format(formatter), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(formatMoney(expense.amountInCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { onDeleteExpense(expense) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir gasto") }
        }
    }
}

private fun formatMoney(amountInCents: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return formatter.format(amountInCents / 100.0)
}
