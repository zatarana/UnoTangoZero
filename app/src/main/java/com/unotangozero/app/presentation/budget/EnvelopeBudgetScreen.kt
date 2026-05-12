package com.unotangozero.app.presentation.budget

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
    val snackbarHostState = remember { SnackbarHostState() }

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

@OptIn(ExperimentalMaterial3Api::class)
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
    var isFormOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Orçamento", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Acompanhe seus envelopes e o quanto ainda pode distribuir.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item { BudgetSummaryCard(summary) }
            item { Text("Envelopes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) }
            if (summary.envelopes.isEmpty()) {
                item { EmptyBudgetCard("Nenhum envelope criado para este mês. Toque no + para criar o primeiro.") }
            } else {
                items(summary.envelopes, key = { it.envelope.id }) { status ->
                    EnvelopeStatusCard(status = status, onDeleteEnvelope = onDeleteEnvelope)
                }
            }
        }

        FloatingActionButton(
            onClick = { isFormOpen = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Novo envelope")
        }
    }

    if (isFormOpen) {
        ModalBottomSheet(
            onDismissRequest = { isFormOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            EnvelopeFormSheet(
                form = form,
                categories = categories,
                onCategoryChange = onCategoryChange,
                onCategorySelected = onCategorySelected,
                onAmountChange = onAmountChange,
                onRolloverChange = onRolloverChange,
                onSaveEnvelope = {
                    onSaveEnvelope()
                    isFormOpen = false
                },
                onClose = { isFormOpen = false }
            )
        }
    }
}

@Composable
private fun BudgetSummaryCard(summary: MonthlyBudgetSummary) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo de ${summary.yearMonth}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(money(summary.amountToDistributeInCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("a distribuir", color = MaterialTheme.colorScheme.onSurfaceVariant)
            SummaryLine("Receitas do mês", summary.totalIncomeInCents)
            SummaryLine("Orçado", summary.totalAllocatedInCents)
            SummaryLine("Disponível", summary.totalAvailableInCents)
            SummaryLine("Gasto", summary.totalSpentInCents)
            if (summary.totalRolloverInCents > 0L) SummaryLine("Sobra herdada", summary.totalRolloverInCents)
        }
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
private fun EnvelopeFormSheet(
    form: EnvelopeBudgetFormState,
    categories: List<FinancialCategory>,
    onCategoryChange: (String) -> Unit,
    onCategorySelected: (FinancialCategory) -> Unit,
    onAmountChange: (String) -> Unit,
    onRolloverChange: (Boolean) -> Unit,
    onSaveEnvelope: () -> Unit,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Novo envelope", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.category,
            onValueChange = onCategoryChange,
            label = { Text("Categoria") },
            placeholder = { Text("Ex: alimentação") },
            singleLine = true
        )
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
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.amountText,
            onValueChange = onAmountChange,
            label = { Text("Valor orçado") },
            prefix = { Text("R$ ") },
            singleLine = true
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = form.rolloverEnabled, onCheckedChange = onRolloverChange)
            Text("Sobra acumula para o próximo mês")
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveEnvelope) { Text("Criar envelope") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Cancelar") }
    }
}

@Composable
private fun EnvelopeStatusCard(status: BudgetEnvelopeStatus, onDeleteEnvelope: (String) -> Unit) {
    val progress = (status.percentageUsed / 100.0).toFloat().coerceIn(0f, 1f)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(status.envelope.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(if (status.envelope.rolloverEnabled) "Sobra acumula" else "Sobra volta ao disponível", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onDeleteEnvelope(status.envelope.id) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            SummaryLine("Orçado", status.envelope.allocatedAmountInCents)
            if (status.rolloverAmountInCents > 0L) SummaryLine("Sobra herdada", status.rolloverAmountInCents)
            SummaryLine("Disponível", status.availableAmountInCents)
            SummaryLine("Gasto", status.spentAmountInCents)
            SummaryLine("Restante", status.remainingAmountInCents)
            if (status.isOverBudget) Text("Categoria estourada", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyBudgetCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
