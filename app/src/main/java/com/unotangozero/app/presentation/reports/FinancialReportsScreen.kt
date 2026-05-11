package com.unotangozero.app.presentation.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun FinancialReportsRoute(viewModel: FinancialReportsViewModel = hiltViewModel()) {
    val report by viewModel.report.collectAsState()
    FinancialReportsScreen(
        report = report,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onCurrentMonth = viewModel::currentMonth
    )
}

@Composable
fun FinancialReportsScreen(
    report: MonthlyFinancialReport,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Relatórios financeiros", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Resumo mensal, gastos por categoria e movimentações do período.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { MonthSelector(report = report, onPreviousMonth = onPreviousMonth, onNextMonth = onNextMonth, onCurrentMonth = onCurrentMonth) }
        item { SummaryCard(report) }
        item { Text("Gastos por categoria", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (report.categoryExpenses.isEmpty()) {
            item { Text("Nenhuma despesa com categoria neste mês.") }
        } else {
            items(report.categoryExpenses, key = { it.category }) { item -> CategoryCard(item) }
        }
        item { Text("Movimentações do mês", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (report.movements.isEmpty()) {
            item { Text("Nenhuma movimentação neste mês.") }
        } else {
            items(report.movements, key = { it.id }) { movement -> MovementReportCard(movement) }
        }
    }
}

@Composable
private fun MonthSelector(
    report: MonthlyFinancialReport,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit
) {
    val locale = Locale("pt", "BR")
    val label = "${report.yearMonth.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase(locale) }} ${report.yearMonth.year}"
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousMonth) { Icon(Icons.Default.ChevronLeft, contentDescription = "Mês anterior") }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = onCurrentMonth) { Text("Mês atual") }
            }
            IconButton(onClick = onNextMonth) { Icon(Icons.Default.ChevronRight, contentDescription = "Próximo mês") }
        }
    }
}

@Composable
private fun SummaryCard(report: MonthlyFinancialReport) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SummaryLine("Receitas", report.incomeInCents)
            SummaryLine("Despesas", report.expenseInCents)
            SummaryLine("Transferências", report.transferInCents)
            SummaryLine("Saldo do mês", report.balanceInCents)
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(money(value), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CategoryCard(item: CategoryExpenseReport) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.category, fontWeight = FontWeight.Bold)
                Text(money(item.amountInCents), fontWeight = FontWeight.SemiBold)
            }
            LinearProgressIndicator(progress = { (item.percentage / 100.0).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
            Text("${item.percentage.toInt()}% das despesas")
        }
    }
}

@Composable
private fun MovementReportCard(movement: FinancialMovement) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(movement.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${movement.type.displayName} • ${movement.date.format(formatter)}${movement.category?.let { " • $it" } ?: ""}")
            Text(money(movement.amountInCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
