package com.unotangozero.app.presentation.budget

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.BudgetEnvelopeStatus
import com.unotangozero.app.domain.models.FinancialCategory
import com.unotangozero.app.domain.models.MonthlyBudgetSummary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun EnvelopeBudgetRoute(viewModel: EnvelopeBudgetViewModel = hiltViewModel()) {
    val summary by viewModel.summary.collectAsState()
    val form by viewModel.form.collectAsState()
    val categories by viewModel.expenseCategories.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Column(Modifier.fillMaxSize()) {
        SnackbarHost(snackbarHostState)
        EnvelopeBudgetScreen(
            summary = summary,
            form = form,
            categories = categories,
            onCategoryChange = viewModel::onCategoryChange,
            onCategorySelected = viewModel::onCategorySelected,
            onAmountChange = viewModel::onAmountChange,
            onRolloverChange = viewModel::onRolloverChange,
            onSaveEnvelope = viewModel::saveEnvelope,
            onDeleteEnvelope = viewModel::deleteEnvelope
        )
    }
}

@Composable
fun EnvelopeBudgetScreen(
    summary: MonthlyBudgetSummary,
    form: EnvelopeBudgetFormState,
    categories: List<FinancialCategory>,
    onCategoryChange: (String) -> Unit,
    onCategorySelected: (FinancialCategory) -> Unit,
    onAmountChange: (String) -> Unit,
    onRolloverChange: (Boolean) -> Unit,
    onSaveEnvelope: () -> Unit,
    onDeleteEnvelope: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Orçamento", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Distribua sua receita mensal em envelopes por categoria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { BudgetSummaryCard(summary) }
        item {
            EnvelopeFormCard(
                form = form,
                categories = categories,
                onCategoryChange = onCategoryChange,
                onCategorySelected = onCategorySelected,
                onAmountChange = onAmountChange,
                onRolloverChange = onRolloverChange,
                onSaveEnvelope = onSaveEnvelope
            )
        }
        item { Text("Envelopes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (summary.envelopes.isEmpty()) {
            item { Text("Nenhum envelope criado para este mês.") }
        } else {
            items(summary.envelopes, key = { it.envelope.id }) { status ->
                EnvelopeStatusCard(status = status, onDeleteEnvelope = onDeleteEnvelope)
            }
        }
    }
}

@Composable
private fun BudgetSummaryCard(summary: MonthlyBudgetSummary) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo de ${summary.yearMonth}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SummaryLine("Receitas do mês", summary.totalIncomeInCents)
            SummaryLine("Orçado em envelopes", summary.totalAllocatedInCents)
            SummaryLine("Sobra herdada", summary.totalRolloverInCents)
            SummaryLine("Disponível nos envelopes", summary.totalAvailableInCents)
            SummaryLine("Gasto no mês", summary.totalSpentInCents)
            SummaryLine("A distribuir", summary.amountToDistributeInCents)
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
private fun EnvelopeFormCard(
    form: EnvelopeBudgetFormState,
    categories: List<FinancialCategory>,
    onCategoryChange: (String) -> Unit,
    onCategorySelected: (FinancialCategory) -> Unit,
    onAmountChange: (String) -> Unit,
    onRolloverChange: (Boolean) -> Unit,
    onSaveEnvelope: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Novo envelope", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.category, onValueChange = onCategoryChange, label = { Text("Categoria") }, placeholder = { Text("Ex: alimentação") }, singleLine = true)
            if (categories.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories, key = { it.id }) { category ->
                        FilterChip(
                            selected = form.category == category.displayLabel,
                            onClick = { onCategorySelected(category) },
                            label = { Text(category.displayLabel) }
                        )
                    }
                }
            }
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.amountText, onValueChange = onAmountChange, label = { Text("Valor orçado") }, prefix = { Text("R$ ") }, singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = form.rolloverEnabled, onCheckedChange = onRolloverChange)
                Text("Sobra acumula para o próximo mês")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveEnvelope) { Text("Criar envelope") }
        }
    }
}

@Composable
private fun EnvelopeStatusCard(status: BudgetEnvelopeStatus, onDeleteEnvelope: (String) -> Unit) {
    val progress = (status.percentageUsed / 100.0).toFloat().coerceIn(0f, 1f)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(status.envelope.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(if (status.envelope.rolloverEnabled) "Rollover ativo" else "Rollover desligado")
                }
                IconButton(onClick = { onDeleteEnvelope(status.envelope.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir")
                }
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Text("Orçado: ${money(status.envelope.allocatedAmountInCents)}")
            if (status.rolloverAmountInCents > 0L) Text("Sobra herdada: ${money(status.rolloverAmountInCents)}")
            Text("Disponível: ${money(status.availableAmountInCents)}")
            Text("Gasto: ${money(status.spentAmountInCents)}")
            Text("Restante: ${money(status.remainingAmountInCents)}", fontWeight = FontWeight.SemiBold)
            if (status.isOverBudget) Text("Categoria estourada", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
