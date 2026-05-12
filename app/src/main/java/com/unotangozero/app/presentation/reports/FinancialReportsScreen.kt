package com.unotangozero.app.presentation.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportsScreen(
    report: MonthlyFinancialReport,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit
) {
    var isExportOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Relatórios", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Resumo do mês, categorias e histórico em uma visão limpa.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { MonthSelector(report = report, onPreviousMonth = onPreviousMonth, onNextMonth = onNextMonth, onCurrentMonth = onCurrentMonth) }
            item { SummaryHeroCard(report) }

            item { Text("Gastos por categoria", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) }
            if (report.categoryExpenses.isEmpty()) {
                item { EmptyReportCard("Nenhuma despesa com categoria neste mês.") }
            } else {
                items(report.categoryExpenses, key = { it.category }) { item -> CategoryCard(item) }
            }

            item { Text("Movimentações do mês", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) }
            if (report.movements.isEmpty()) {
                item { EmptyReportCard("Nenhuma movimentação neste mês.") }
            } else {
                items(report.movements, key = { it.id }) { movement -> MovementReportCard(movement) }
            }
        }

        FloatingActionButton(
            onClick = { isExportOpen = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ações do relatório")
        }
    }

    if (isExportOpen) {
        ModalBottomSheet(
            onDismissRequest = { isExportOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            ReportActionsSheet(
                report = report,
                onCurrentMonth = {
                    onCurrentMonth()
                    isExportOpen = false
                },
                onClose = { isExportOpen = false }
            )
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text("Toque nas setas para navegar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onNextMonth) { Icon(Icons.Default.ChevronRight, contentDescription = "Próximo mês") }
        }
    }
}

@Composable
private fun SummaryHeroCard(report: MonthlyFinancialReport) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Saldo do mês", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Text(money(report.balanceInCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniReportCard(modifier = Modifier.weight(1f), title = "Receitas", value = report.incomeInCents)
                MiniReportCard(modifier = Modifier.weight(1f), title = "Despesas", value = report.expenseInCents)
            }
            SummaryLine("Transferências", report.transferInCents)
            SummaryLine("Ajustes", report.adjustmentInCents)
        }
    }
}

@Composable
private fun MiniReportCard(modifier: Modifier = Modifier, title: String, value: Long) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(money(value), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ReportActionsSheet(report: MonthlyFinancialReport, onCurrentMonth: () -> Unit, onClose: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val csv = remember(report) { buildCsv(report) }
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ações do relatório", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text("Copie o CSV do mês para colar no Google Sheets, Excel ou bloco de notas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { clipboardManager.setText(AnnotatedString(csv)) }
        ) {
            Text("Copiar CSV do mês")
        }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onCurrentMonth) { Text("Voltar para mês atual") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Fechar") }
    }
}

@Composable
private fun SummaryLine(label: String, value: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(money(value), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CategoryCard(item: CategoryExpenseReport) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(money(item.amountInCents), fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(progress = { (item.percentage / 100.0).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
            Text("${item.percentage.toInt()}% das despesas", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MovementReportCard(movement: FinancialMovement) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(movement.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("${movement.type.displayName} • ${movement.date.format(formatter)}${movement.category?.let { " • $it" } ?: ""}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(money(movement.amountInCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyReportCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun buildCsv(report: MonthlyFinancialReport): String {
    val builder = StringBuilder()
    builder.appendLine("tipo;data;descricao;categoria;valor")
    builder.appendLine("Resumo;;;Receitas;${formatCents(report.incomeInCents)}")
    builder.appendLine("Resumo;;;Despesas;${formatCents(report.expenseInCents)}")
    builder.appendLine("Resumo;;;Transferencias;${formatCents(report.transferInCents)}")
    builder.appendLine("Resumo;;;Ajustes;${formatCents(report.adjustmentInCents)}")
    builder.appendLine("Resumo;;;Saldo do mes;${formatCents(report.balanceInCents)}")
    report.categoryExpenses.forEach { item ->
        builder.appendLine("Categoria;;;${escapeCsv(item.category)};${formatCents(item.amountInCents)}")
    }
    report.movements.forEach { movement ->
        builder.appendLine(
            listOf(
                movement.type.displayName,
                movement.date.toString(),
                movement.description,
                movement.category.orEmpty(),
                formatCents(movement.amountInCents)
            ).joinToString(";") { escapeCsv(it) }
        )
    }
    return builder.toString()
}

private fun escapeCsv(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return if (escaped.contains(';') || escaped.contains('\n') || escaped.contains('"')) "\"$escaped\"" else escaped
}

private fun formatCents(cents: Long): String {
    return "%.2f".format(Locale.US, cents / 100.0).replace('.', ',')
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
