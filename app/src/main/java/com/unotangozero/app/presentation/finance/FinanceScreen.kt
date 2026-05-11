package com.unotangozero.app.presentation.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.PlannedBill
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FinanceRoute(viewModel: FinanceDashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    FinanceScreen(uiState)
}

@Composable
fun FinanceScreen(uiState: FinanceDashboardUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Finanças", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Painel central com saldo, mês atual, orçamento e próximos vencimentos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item { BalanceHeroCard(uiState) }
        item { MonthSummaryCard(uiState) }
        item { BudgetPreviewCard(uiState) }
        item { ProjectionPreviewCard(uiState) }

        if (uiState.overdueBills.isNotEmpty()) {
            item { SectionTitle("Vencidas") }
            items(uiState.overdueBills, key = { it.id }) { bill -> BillPreviewCard(bill) }
        }

        item { SectionTitle("Próximos 30 dias") }
        if (uiState.upcomingBills.isEmpty()) {
            item { EmptyCard("Nenhuma conta planejada para os próximos 30 dias.") }
        } else {
            items(uiState.upcomingBills, key = { it.id }) { bill -> BillPreviewCard(bill) }
        }

        item {
            EmptyCard("Use a aba Mais para acessar: Contas, Movimentações, Orçamento, Relatórios, Metas, Reconciliação e Projeção de saldo.")
        }
    }
}

@Composable
private fun BalanceHeroCard(uiState: FinanceDashboardUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Saldo total atual", style = MaterialTheme.typography.labelLarge)
            Text(money(uiState.totalBalanceInCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Considera contas, movimentações, transferências e ajustes de reconciliação.")
        }
    }
}

@Composable
private fun MonthSummaryCard(uiState: FinanceDashboardUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo do mês", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SummaryLine("Receitas", uiState.monthlyIncomeInCents)
            SummaryLine("Despesas", uiState.monthlyExpenseInCents)
            SummaryLine("Ajustes", uiState.monthlyAdjustmentInCents)
            SummaryLine("Resultado", uiState.monthlyBalanceInCents)
        }
    }
}

@Composable
private fun BudgetPreviewCard(uiState: FinanceDashboardUiState) {
    val budget = uiState.budgetSummary
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Orçamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (budget == null || budget.envelopes.isEmpty()) {
                Text("Nenhum envelope criado para este mês.")
            } else {
                SummaryLine("Disponível", budget.totalAvailableInCents)
                SummaryLine("Gasto", budget.totalSpentInCents)
                SummaryLine("A distribuir", budget.amountToDistributeInCents)
                val progress = if (budget.totalAvailableInCents > 0L) {
                    (budget.totalSpentInCents.toDouble() / budget.totalAvailableInCents.toDouble()).toFloat().coerceIn(0f, 1f)
                } else 0f
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ProjectionPreviewCard(uiState: FinanceDashboardUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Projeção próxima", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SummaryLine("Impacto 30 dias", uiState.next30DaysImpactInCents)
            SummaryLine("Saldo estimado", uiState.totalBalanceInCents + uiState.next30DaysImpactInCents)
        }
    }
}

@Composable
private fun BillPreviewCard(bill: PlannedBill) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(bill.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${bill.type.displayName} • ${bill.dueDate.format(formatter)}${bill.category?.let { " • $it" } ?: ""}")
            Text(money(bill.amountInCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(16.dp))
    }
}

@Composable
private fun SummaryLine(label: String, value: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(money(value), fontWeight = FontWeight.SemiBold)
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
