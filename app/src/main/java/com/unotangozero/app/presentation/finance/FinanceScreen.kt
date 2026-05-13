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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import com.unotangozero.app.domain.models.PlannedBill
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FinanceRoute(
    onOpenAccounts: () -> Unit,
    onOpenMovements: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenProjection: () -> Unit,
    onOpenReconciliation: () -> Unit,
    onOpenCategories: () -> Unit,
    viewModel: FinanceDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    FinanceScreen(
        uiState = uiState,
        onOpenAccounts = onOpenAccounts,
        onOpenMovements = onOpenMovements,
        onOpenBudget = onOpenBudget,
        onOpenGoals = onOpenGoals,
        onOpenReports = onOpenReports,
        onOpenProjection = onOpenProjection,
        onOpenReconciliation = onOpenReconciliation,
        onOpenCategories = onOpenCategories
    )
}

@Composable
fun FinanceScreen(
    uiState: FinanceDashboardUiState,
    onOpenAccounts: () -> Unit,
    onOpenMovements: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenProjection: () -> Unit,
    onOpenReconciliation: () -> Unit,
    onOpenCategories: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Finanças", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Resumo do seu dinheiro, orçamento, lançamentos e projeções.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { BalanceHeroCard(uiState, onOpenAccounts) }
            item { FinanceQuickAccessRow(onOpenAccounts, onOpenBudget, onOpenReports, onOpenProjection, onOpenGoals, onOpenReconciliation, onOpenCategories) }
            item { MonthSummaryCard(uiState) }
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

        FloatingActionButton(
            onClick = onOpenMovements,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Novo lançamento")
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
    onOpenReconciliation: () -> Unit,
    onOpenCategories: () -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected = false, onClick = onOpenAccounts, label = { Text("Contas") }) }
        item { FilterChip(selected = false, onClick = onOpenBudget, label = { Text("Orçamento") }) }
        item { FilterChip(selected = false, onClick = onOpenReports, label = { Text("Relatórios") }) }
        item { FilterChip(selected = false, onClick = onOpenProjection, label = { Text("Projeção") }) }
        item { FilterChip(selected = false, onClick = onOpenGoals, label = { Text("Metas") }) }
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
                Text("Nenhum lançamento ainda. Toque para cadastrar receita, despesa, transferência ou ajuste.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
