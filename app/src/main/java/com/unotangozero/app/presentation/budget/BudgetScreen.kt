package com.unotangozero.app.presentation.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.BudgetEnvelope
import com.unotangozero.app.domain.models.BudgetEnvelopeStatus
import com.unotangozero.app.domain.models.FinancialMovement
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BudgetRoute(viewModel: BudgetViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
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
        BudgetScreen(
            uiState = uiState,
            formState = formState,
            onCategoryChange = viewModel::onCategoryChange,
            onPlannedAmountChange = viewModel::onPlannedAmountChange,
            onRolloverChange = viewModel::onRolloverChange,
            onSaveEnvelope = viewModel::saveEnvelope,
            onCancelEditing = viewModel::cancelEditing,
            onStartEditing = viewModel::startEditing,
            onDeleteEnvelope = viewModel::deleteEnvelope
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    uiState: BudgetUiState,
    formState: BudgetEnvelopeFormUiState,
    onCategoryChange: (String) -> Unit,
    onPlannedAmountChange: (String) -> Unit,
    onRolloverChange: (Boolean) -> Unit,
    onSaveEnvelope: () -> Unit,
    onCancelEditing: () -> Unit,
    onStartEditing: (BudgetEnvelope) -> Unit,
    onDeleteEnvelope: (BudgetEnvelope) -> Unit
) {
    var isSheetOpen by remember { mutableStateOf(false) }
    var selectedEnvelope by remember { mutableStateOf<BudgetEnvelope?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(formState.isEditing) {
        if (formState.isEditing) isSheetOpen = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Orçamento", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Organize o mês com envelopes: defina quanto cada categoria pode gastar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { BudgetSummaryCard(uiState) }
            item { EnvelopeGuidanceCard() }

            item { SectionTitle("Envelopes de ${uiState.yearMonth}", uiState.currentMonthEnvelopes.size) }
            if (uiState.currentMonthEnvelopes.isEmpty()) {
                item { EmptyBudgetCard() }
            } else {
                val statusesByEnvelopeId = uiState.summary?.envelopes.orEmpty().associateBy { it.envelope.id }
                items(uiState.currentMonthEnvelopes, key = { it.id }) { envelope ->
                    EnvelopeCard(
                        envelope = envelope,
                        status = statusesByEnvelopeId[envelope.id],
                        onOpenDetails = { selectedEnvelope = envelope },
                        onStartEditing = onStartEditing,
                        onDeleteEnvelope = onDeleteEnvelope
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { isSheetOpen = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Novo envelope")
        }
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                onCancelEditing()
                isSheetOpen = false
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            EnvelopeFormSheet(
                formState = formState,
                onCategoryChange = onCategoryChange,
                onPlannedAmountChange = onPlannedAmountChange,
                onRolloverChange = onRolloverChange,
                onSaveEnvelope = {
                    onSaveEnvelope()
                    isSheetOpen = false
                },
                onCancel = {
                    onCancelEditing()
                    isSheetOpen = false
                }
            )
        }
    }

    selectedEnvelope?.let { envelope ->
        val relatedTransactions = remember(envelope.id, uiState.currentMonthExpenseMovements) {
            uiState.currentMonthExpenseMovements.filter { movement ->
                movement.category?.trim()?.equals(envelope.category.trim(), ignoreCase = true) == true
            }
        }
        ModalBottomSheet(
            onDismissRequest = { selectedEnvelope = null },
            sheetState = detailSheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            EnvelopeTransactionsSheet(
                envelope = envelope,
                transactions = relatedTransactions,
                onClose = { selectedEnvelope = null }
            )
        }
    }
}

@Composable
private fun BudgetSummaryCard(uiState: BudgetUiState) {
    val summary = uiState.summary
    val allocated = summary?.totalAllocatedInCents ?: uiState.currentMonthEnvelopes.sumOf { it.allocatedAmountInCents }
    val spent = summary?.totalSpentInCents ?: 0L
    val income = summary?.totalIncomeInCents ?: 0L
    val toDistribute = summary?.amountToDistributeInCents ?: -allocated
    val progress = if (allocated > 0L) (spent.toDouble() / allocated.toDouble()).toFloat().coerceIn(0f, 1f) else 0f

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo do mês", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("Planejado: ${money(allocated)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            SummaryLine("Receita do mês", income)
            SummaryLine("Gasto até agora", spent)
            SummaryLine("A distribuir", toDistribute)
        }
    }
}

@Composable
private fun EnvelopeGuidanceCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Como funciona", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Cada envelope representa uma categoria de orçamento do mês, como Alimentação, Transporte ou Lazer.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Toque em um envelope para ver as transações relacionadas à categoria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EnvelopeCard(
    envelope: BudgetEnvelope,
    status: BudgetEnvelopeStatus?,
    onOpenDetails: () -> Unit,
    onStartEditing: (BudgetEnvelope) -> Unit,
    onDeleteEnvelope: (BudgetEnvelope) -> Unit
) {
    val spent = status?.spentAmountInCents ?: 0L
    val available = status?.availableAmountInCents ?: envelope.allocatedAmountInCents
    val remaining = status?.remainingAmountInCents ?: (available - spent)
    val rawProgress = if (available > 0L) (spent.toDouble() / available.toDouble()).toFloat() else 0f
    val progress = rawProgress.coerceIn(0f, 1f)
    val isOverBudget = status?.isOverBudget == true || remaining < 0L
    val progressColor = budgetProgressColor(rawProgress)

    Card(
        modifier = Modifier.clickable(onClick = onOpenDetails),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(envelope.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Planejado: ${money(envelope.allocatedAmountInCents)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onStartEditing(envelope) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar envelope")
                }
                IconButton(onClick = { onDeleteEnvelope(envelope) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir envelope")
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.18f)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Gasto: ${money(spent)}")
                Text(if (isOverBudget) "Estourou: ${money(-remaining)}" else "Restante: ${money(remaining)}", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, label = { Text("${(rawProgress * 100).toInt()}% usado") })
                AssistChip(onClick = {}, label = { Text(progressLabel(rawProgress)) })
                if (envelope.rolloverEnabled) AssistChip(onClick = {}, label = { Text("Sobra acumulável") })
                if (isOverBudget) AssistChip(onClick = {}, label = { Text("Acima do orçamento") })
            }
        }
    }
}

@Composable
private fun EnvelopeTransactionsSheet(
    envelope: BudgetEnvelope,
    transactions: List<FinancialMovement>,
    onClose: () -> Unit
) {
    val total = transactions.sumOf { it.amountInCents }
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Transações de ${envelope.category}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text("Total gasto no mês: ${money(total)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (transactions.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    "Nenhuma despesa deste mês encontrada para esta categoria.",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(transactions, key = { it.id }) { movement ->
                    RelatedTransactionRow(movement)
                }
            }
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onClose) {
            Text("Fechar")
        }
    }
}

@Composable
private fun RelatedTransactionRow(movement: FinancialMovement) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(movement.description, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(movement.date.format(formatter), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(money(movement.amountInCents), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun EnvelopeFormSheet(
    formState: BudgetEnvelopeFormUiState,
    onCategoryChange: (String) -> Unit,
    onPlannedAmountChange: (String) -> Unit,
    onRolloverChange: (Boolean) -> Unit,
    onSaveEnvelope: () -> Unit,
    onCancel: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (formState.isEditing) "Editar envelope" else "Novo envelope", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text("Defina uma categoria e o valor planejado para este mês.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = formState.category,
            onValueChange = onCategoryChange,
            label = { Text("Categoria") },
            placeholder = { Text("Ex: Alimentação, Transporte, Lazer") },
            singleLine = true
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = formState.plannedAmountText,
            onValueChange = onPlannedAmountChange,
            label = { Text("Valor planejado do mês") },
            prefix = { Text("R$ ") },
            singleLine = true
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Acumular sobra", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Leva o saldo não usado para o próximo mês.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = formState.rolloverEnabled, onCheckedChange = onRolloverChange)
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveEnvelope) {
            Text(if (formState.isEditing) "Salvar alterações" else "Criar envelope")
        }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onCancel) {
            Text("Cancelar")
        }
    }
}

@Composable
private fun EmptyBudgetCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nenhum envelope criado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Toque no + para criar seu primeiro envelope e planejar o mês por categoria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionTitle(title: String, count: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        AssistChip(onClick = {}, label = { Text(count.toString()) })
    }
}

@Composable
private fun SummaryLine(label: String, value: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(money(value), fontWeight = FontWeight.Bold)
    }
}

private fun budgetProgressColor(progress: Float): Color = when {
    progress < 0.70f -> Color(0xFF2E7D32)
    progress < 1.0f -> Color(0xFFFFA000)
    else -> Color(0xFFC62828)
}

private fun progressLabel(progress: Float): String = when {
    progress < 0.70f -> "Seguro"
    progress < 1.0f -> "Atenção"
    else -> "Vermelho"
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
