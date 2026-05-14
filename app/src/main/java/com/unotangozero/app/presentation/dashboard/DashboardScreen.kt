package com.unotangozero.app.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.presentation.common.UiState
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardRoute(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    when (val currentState = state) {
        UiState.Loading -> DashboardLoadingState()
        is UiState.Error -> DashboardErrorState(currentState.message)
        is UiState.Success -> DashboardScreen(currentState.data)
    }
}

@Composable
fun DashboardScreen(state: DashboardUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Dashboard", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                Text(
                    text = "Visão geral de finanças, tarefas, hábitos e metas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            DashboardMetricCard(
                title = "Saldo atual",
                value = formatMoney(state.currentBalanceInCents),
                description = "Soma do saldo atual das contas cadastradas."
            )
        }

        item { MonthlyExpensesPieCard(slices = state.monthlyExpenseSlices) }

        item { UpcomingBillsCard(bills = state.upcomingBills) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Tarefas do dia",
                    value = state.todayTasksCount.toString(),
                    description = "pendente(s) hoje"
                )
                DashboardMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Streak de hábitos",
                    value = state.maxHabitStreak.toString(),
                    description = "maior sequência ativa"
                )
            }
        }

        item { GoalsProgressCard(progressPercent = state.averageGoalsProgressPercent) }
    }
}

@Composable
private fun DashboardLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularProgressIndicator()
            Text("Carregando Dashboard...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DashboardErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Não foi possível carregar o Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(message)
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MonthlyExpensesPieCard(slices: List<ExpenseCategorySliceUi>) {
    val total = slices.sumOf { it.amountInCents }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Despesas do mês por categoria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (slices.isEmpty() || total <= 0L) {
                Text("Nenhuma despesa registrada neste mês.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExpensePieChart(
                        slices = slices,
                        modifier = Modifier.size(150.dp)
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        slices.take(5).forEachIndexed { index, slice ->
                            ExpenseLegendRow(
                                slice = slice,
                                color = pieColor(index),
                                totalInCents = total
                            )
                        }
                    }
                }
                Text("Total no mês: ${formatMoney(total)}", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ExpensePieChart(
    slices: List<ExpenseCategorySliceUi>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.amountInCents }.toFloat().coerceAtLeast(1f)
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.22f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        var startAngle = -90f
        slices.forEachIndexed { index, slice ->
            val sweepAngle = (slice.amountInCents / total) * 360f
            drawArc(
                color = pieColor(index),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun ExpenseLegendRow(
    slice: ExpenseCategorySliceUi,
    color: Color,
    totalInCents: Long
) {
    val percent = if (totalInCents > 0L) ((slice.amountInCents * 100) / totalInCents).toInt() else 0
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(slice.category, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text("$percent% • ${formatMoney(slice.amountInCents)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun UpcomingBillsCard(bills: List<UpcomingBillUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Próximas contas a pagar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (bills.isEmpty()) {
                Text("Nenhuma conta futura encontrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                bills.forEach { bill -> UpcomingBillRow(bill) }
            }
        }
    }
}

@Composable
private fun UpcomingBillRow(bill: UpcomingBillUi) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(bill.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text("Vence em ${bill.dueDate.format(formatter)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(formatMoney(bill.amountInCents), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GoalsProgressCard(progressPercent: Int) {
    val progress = (progressPercent / 100f).coerceIn(0f, 1f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Progresso das metas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("$progressPercent%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Text("Porcentagem média das metas cadastradas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun pieColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF6750A4),
        Color(0xFF006A6A),
        Color(0xFFB3261E),
        Color(0xFF7D5260),
        Color(0xFF386A20),
        Color(0xFF8C5000),
        Color(0xFF005DBA),
        Color(0xFF6D5E00)
    )
    return colors[index % colors.size]
}

private fun formatMoney(amountInCents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(amountInCents / 100.0)
}
