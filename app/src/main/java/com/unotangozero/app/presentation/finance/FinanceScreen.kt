package com.unotangozero.app.presentation.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import com.unotangozero.app.domain.models.PlannedBill
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FinanceRoute(
    onOpenAccounts: () -> Unit,
    onOpenMovements: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenDebts: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenProjection: () -> Unit,
    onOpenReconciliation: () -> Unit,
    onOpenCategories: () -> Unit,
    viewModel: FinanceDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val currentMessage = message
        if (currentMessage != null) {
            snackbarHostState.showSnackbar(currentMessage)
            viewModel.clearMessage()
        }
    }

    FinanceScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onOpenAccounts = onOpenAccounts,
        onOpenMovements = onOpenMovements,
        onOpenBudget = onOpenBudget,
        onOpenGoals = onOpenGoals,
        onOpenDebts = onOpenDebts,
        onOpenReports = onOpenReports,
        onOpenProjection = onOpenProjection,
        onOpenReconciliation = onOpenReconciliation,
        onOpenCategories = onOpenCategories,
        onSaveQuickTransaction = viewModel::registerQuickTransaction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    uiState: FinanceDashboardUiState,
    snackbarHostState: SnackbarHostState,
    onOpenAccounts: () -> Unit,
    onOpenMovements: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenDebts: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenProjection: () -> Unit,
    onOpenReconciliation: () -> Unit,
    onOpenCategories: () -> Unit,
    onSaveQuickTransaction: (String, FinancialMovementType, String, String?, LocalDate) -> Unit
) {
    var isQuickSheetOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Finanças", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Resumo do seu dinheiro, orçamento, dívidas, lançamentos e projeções.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { BalanceHeroCard(uiState, onOpenAccounts) }
            item { FinanceQuickAccessRow(onOpenAccounts, onOpenBudget, onOpenReports, onOpenProjection, onOpenGoals, onOpenDebts, onOpenReconciliation, onOpenCategories) }
            item { MonthSummaryCard(uiState) }
            item { FinanceDebtsPreviewCard(onOpenDebts) }
            item { RecentMovementsCard(uiState, onOpenMovements) }
            item { BudgetPreviewCard(uiState, onOpenBudget) }
            item { ProjectionPreviewCard(uiState, onOpenProjection) }

            if (uiState.overdueBills.isNotEmpty()) {
                item { SectionTitle("Vencidas") }
                items(uiState.overdueBills, key = { it.id }) { bill -> BillPreviewCard(bill) }
            }

            item { SectionTitle("Próximos 30 dias") }
            if (uiState.upcomingBills.isEmpty()) {
                item { EmptyCard("Nenhum vencimento previsto para os próximos 30 dias.") }
            } else {
                items(uiState.upcomingBills, key = { it.id }) { bill -> BillPreviewCard(bill) }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp))

        FloatingActionButton(
            onClick = { isQuickSheetOpen = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Novo lançamento")
        }
    }

    if (isQuickSheetOpen) {
        QuickTransactionSheet(
            uiState = uiState,
            onDismiss = { isQuickSheetOpen = false },
            onSave = { amountText, type, category, accountId, date ->
                onSaveQuickTransaction(amountText, type, category, accountId, date)
                isQuickSheetOpen = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickTransactionSheet(
    uiState: FinanceDashboardUiState,
    onDismiss: () -> Unit,
    onSave: (String, FinancialMovementType, String, String?, LocalDate) -> Unit
) {
    val categories = remember { listOf("Alimentação", "Transporte", "Moradia", "Saúde", "Educação", "Lazer", "Salário", "Dívidas", "Outros") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(FinancialMovementType.EXPENSE) }
    var category by remember { mutableStateOf(categories.first()) }
    var accountId by remember(uiState.accounts) { mutableStateOf(uiState.accounts.firstOrNull()?.id) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.toEpochMillis())
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val selectedAccountName = uiState.accounts.firstOrNull { it.id == accountId }?.name ?: "Selecione uma conta"

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Novo lançamento rápido", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = amountText,
                onValueChange = { amountText = it.filterMoneyChars() },
                label = { Text("Valor") },
                prefix = { Text("R$ ") },
                singleLine = true
            )
            QuickDropdown(
                label = "Tipo",
                selectedText = type.displayName,
                options = listOf(FinancialMovementType.EXPENSE.displayName, FinancialMovementType.INCOME.displayName),
                onOptionSelected = { selected ->
                    type = if (selected == FinancialMovementType.INCOME.displayName) FinancialMovementType.INCOME else FinancialMovementType.EXPENSE
                }
            )
            QuickDropdown(
                label = "Categoria",
                selectedText = category,
                options = categories,
                onOptionSelected = { category = it }
            )
            QuickDropdown(
                label = "Conta",
                selectedText = selectedAccountName,
                options = uiState.accounts.map { it.name },
                onOptionSelected = { selectedName ->
                    accountId = uiState.accounts.firstOrNull { it.name == selectedName }?.id
                }
            )
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { isDatePickerOpen = true }) {
                Text("Data: ${date.format(formatter)}")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSave(amountText, type, category, accountId, date) }
            ) {
                Text("Salvar lançamento")
            }
        }
    }

    if (isDatePickerOpen) {
        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { date = it.toLocalDate() }
                        isDatePickerOpen = false
                    }
                ) { Text("Selecionar") }
            },
            dismissButton = { TextButton(onClick = { isDatePickerOpen = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun QuickDropdown(
    label: String,
    selectedText: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { expanded = true }) {
            Text("$label: $selectedText")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("Nenhuma opção disponível") }, onClick = { expanded = false })
            } else {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FinanceQuickAccessRow(
    onOpenAccounts: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenProjection: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenDebts: () -> Unit,
    onOpenReconciliation: () -> Unit,
    onOpenCategories: () -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected = false, onClick = onOpenAccounts, label = { Text("Contas") }) }
        item { FilterChip(selected = false, onClick = onOpenBudget, label = { Text("Orçamento") }) }
        item { FilterChip(selected = false, onClick = onOpenReports, label = { Text("Relatórios") }) }
        item { FilterChip(selected = false, onClick = onOpenProjection, label = { Text("Projeção") }) }
        item { FilterChip(selected = false, onClick = onOpenGoals, label = { Text("Metas") }) }
        item { FilterChip(selected = false, onClick = onOpenDebts, label = { Text("Dívidas") }) }
        item { FilterChip(selected = false, onClick = onOpenReconciliation, label = { Text("Reconciliar") }) }
        item { FilterChip(selected = false, onClick = onOpenCategories, label = { Text("Categorias") }) }
    }
}

@Composable
private fun BalanceHeroCard(uiState: FinanceDashboardUiState, onOpenAccounts: () -> Unit) {
    Card(onClick = onOpenAccounts, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Saldo total", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(money(uiState.totalBalanceInCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("${uiState.accountCount} conta(s) cadastrada(s) • toque para gerenciar", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MonthSummaryCard(uiState: FinanceDashboardUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Este mês", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniMoneyCard(modifier = Modifier.weight(1f), title = "Receitas", value = uiState.monthlyIncomeInCents)
                MiniMoneyCard(modifier = Modifier.weight(1f), title = "Despesas", value = uiState.monthlyExpenseInCents)
            }
            SummaryLine("Resultado", uiState.monthlyBalanceInCents)
            SummaryLine("Lançamentos", uiState.monthlyMovementCount.toLong(), isMoney = false)
            if (uiState.monthlyAdjustmentInCents != 0L) SummaryLine("Ajustes", uiState.monthlyAdjustmentInCents)
        }
    }
}

@Composable
private fun MiniMoneyCard(modifier: Modifier = Modifier, title: String, value: Long) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(money(value), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun RecentMovementsCard(uiState: FinanceDashboardUiState, onOpenMovements: () -> Unit) {
    Card(onClick = onOpenMovements, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Últimos lançamentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            if (uiState.recentMovements.isEmpty()) {
                Text("Nenhum lançamento ainda. Use o botão + para cadastrar receita ou despesa rapidamente.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                uiState.recentMovements.forEach { movement -> MovementPreviewRow(movement) }
            }
        }
    }
}

@Composable
private fun MovementPreviewRow(movement: FinancialMovement) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM") }
    val signedAmount = when (movement.type) {
        FinancialMovementType.EXPENSE -> -movement.amountInCents
        else -> movement.amountInCents
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(movement.description, fontWeight = FontWeight.Bold)
            Text("${movement.type.displayName} • ${movement.date.format(formatter)}${movement.category?.let { " • $it" } ?: ""}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Text(money(signedAmount), fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun BudgetPreviewCard(uiState: FinanceDashboardUiState, onOpenBudget: () -> Unit) {
    val budget = uiState.budgetSummary
    Card(onClick = onOpenBudget, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Orçamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            if (budget == null || budget.envelopes.isEmpty()) {
                Text("Nenhum envelope criado para este mês. Toque para começar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                SummaryLine("Disponível", budget.totalAvailableInCents)
                SummaryLine("Gasto", budget.totalSpentInCents)
                SummaryLine("A distribuir", budget.amountToDistributeInCents)
                val progress = if (budget.totalAvailableInCents > 0L) {
                    (budget.totalSpentInCents.toDouble() / budget.totalAvailableInCents.toDouble()).toFloat().coerceIn(0f, 1f)
                } else 0f
                Text("${(progress * 100).toInt()}% usado", fontWeight = FontWeight.Bold)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ProjectionPreviewCard(uiState: FinanceDashboardUiState, onOpenProjection: () -> Unit) {
    Card(onClick = onOpenProjection, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Projeção", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            SummaryLine("Impacto em 30 dias", uiState.next30DaysImpactInCents)
            SummaryLine("Saldo estimado", uiState.totalBalanceInCents + uiState.next30DaysImpactInCents)
        }
    }
}

@Composable
private fun BillPreviewCard(bill: PlannedBill) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(bill.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("${bill.type.displayName} • ${bill.dueDate.format(formatter)}${bill.category?.let { " • $it" } ?: ""}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(money(bill.amountInCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
}

@Composable
private fun EmptyCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SummaryLine(label: String, value: Long, isMoney: Boolean = true) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(if (isMoney) money(value) else value.toString(), fontWeight = FontWeight.Bold)
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}

private fun String.filterMoneyChars(): String = filter { it.isDigit() || it == ',' || it == '.' }

private fun LocalDate.toEpochMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
