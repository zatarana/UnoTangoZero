package com.unotangozero.app.presentation.debts

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.enums.DebtStatus
import com.unotangozero.app.domain.models.Debt
import com.unotangozero.app.domain.models.DebtSummary
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DebtsRoute(viewModel: DebtsViewModel = hiltViewModel()) {
    val debts by viewModel.debts.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val editorState by viewModel.editorState.collectAsState()
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
        DebtsScreen(
            debts = debts,
            summary = summary,
            editorState = editorState,
            onCreditorChange = viewModel::onCreditorChange,
            onAmountChange = viewModel::onAmountChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onPreviousDueDay = viewModel::previousDueDay,
            onNextDueDay = viewModel::nextDueDay,
            onDueDateSelected = viewModel::onDueDateSelected,
            onSaveDebt = viewModel::saveDebtFromEditor,
            onCancelEdit = viewModel::cancelEditing,
            onStartEdit = viewModel::startEditing,
            onMarkAsPaid = viewModel::markAsPaid,
            onDeleteDebt = viewModel::deleteDebt
        )
    }
}

@Composable
fun DebtsScreen(
    debts: List<Debt>,
    summary: DebtSummary,
    editorState: DebtEditorUiState,
    onCreditorChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPreviousDueDay: () -> Unit,
    onNextDueDay: () -> Unit,
    onDueDateSelected: (LocalDate) -> Unit,
    onSaveDebt: () -> Unit,
    onCancelEdit: () -> Unit,
    onStartEdit: (Debt) -> Unit,
    onMarkAsPaid: (Debt) -> Unit,
    onDeleteDebt: (Debt) -> Unit
) {
    val openDebts = remember(debts) { debts.filter { it.status != DebtStatus.PAID }.sortedBy { it.dueDate } }
    val paidDebts = remember(debts) { debts.filter { it.status == DebtStatus.PAID }.sortedByDescending { it.updatedAt } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dívidas", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "Acompanhe dívidas abertas, quitadas e o progresso de pagamento.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { DebtSummaryCard(summary = summary, debts = debts) }

        item {
            DebtEditorCard(
                state = editorState,
                onCreditorChange = onCreditorChange,
                onAmountChange = onAmountChange,
                onDescriptionChange = onDescriptionChange,
                onPreviousDueDay = onPreviousDueDay,
                onNextDueDay = onNextDueDay,
                onDueDateSelected = onDueDateSelected,
                onSaveDebt = onSaveDebt,
                onCancelEdit = onCancelEdit
            )
        }

        if (debts.isEmpty()) {
            item { EmptyDebtsCard() }
        } else {
            item { SectionTitle("Em aberto", openDebts.size) }
            if (openDebts.isEmpty()) {
                item { EmptySectionCard("Nenhuma dívida em aberto. Boa!") }
            } else {
                items(items = openDebts, key = { it.id }) { debt ->
                    DebtCard(
                        debt = debt,
                        onStartEdit = onStartEdit,
                        onMarkAsPaid = onMarkAsPaid,
                        onDeleteDebt = onDeleteDebt
                    )
                }
            }

            item { SectionTitle("Quitadas", paidDebts.size) }
            if (paidDebts.isEmpty()) {
                item { EmptySectionCard("Nenhuma dívida quitada ainda.") }
            } else {
                items(items = paidDebts, key = { it.id }) { debt ->
                    DebtCard(
                        debt = debt,
                        onStartEdit = onStartEdit,
                        onMarkAsPaid = onMarkAsPaid,
                        onDeleteDebt = onDeleteDebt
                    )
                }
            }
        }
    }
}

@Composable
private fun DebtEditorCard(
    state: DebtEditorUiState,
    onCreditorChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPreviousDueDay: () -> Unit,
    onNextDueDay: () -> Unit,
    onDueDateSelected: (LocalDate) -> Unit,
    onSaveDebt: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (state.isEditing) "Editar dívida" else "Nova dívida", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.creditor, onValueChange = onCreditorChange, label = { Text("Credor") }, singleLine = true)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.amountText, onValueChange = onAmountChange, label = { Text("Valor em aberto") }, singleLine = true, prefix = { Text("R$ ") })
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.description, onValueChange = onDescriptionChange, label = { Text("Descrição opcional") }, minLines = 2)
            DueDateSelector(dueDate = state.dueDate, onPreviousDueDay = onPreviousDueDay, onNextDueDay = onNextDueDay, onDueDateSelected = onDueDateSelected)
            Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveDebt) { Text(if (state.isEditing) "Salvar alterações" else "Cadastrar dívida") }
            if (state.isEditing) {
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onCancelEdit) { Text("Cancelar edição") }
            }
        }
    }
}

@Composable
private fun DebtSummaryCard(summary: DebtSummary, debts: List<Debt>) {
    val totalOriginal = debts.sumOf { it.originalAmountInCents }.coerceAtLeast(0L)
    val totalRemaining = debts.filter { it.status != DebtStatus.PAID }.sumOf { it.remainingAmountInCents }.coerceAtLeast(0L)
    val paidAmount = (totalOriginal - totalRemaining).coerceAtLeast(0L)
    val progress = if (totalOriginal > 0L) (paidAmount.toDouble() / totalOriginal.toDouble()).toFloat().coerceIn(0f, 1f) else 0f
    val progressPercent = (progress * 100).toInt()

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo das dívidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(formatMoney(totalRemaining), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("em aberto • $progressPercent% quitado", style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Text("${summary.activeDebts} ativa(s) • ${summary.paidDebts} paga(s)", style = MaterialTheme.typography.bodyMedium)
            summary.nextDueDate?.let {
                Text(
                    text = "Próximo vencimento: ${it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} (${formatMoney(summary.nextDueAmountInCents)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDateSelector(
    dueDate: LocalDate,
    onPreviousDueDay: () -> Unit,
    onNextDueDay: () -> Unit,
    onDueDateSelected: (LocalDate) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    var isCalendarOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate.toEpochMillis())

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousDueDay) { Icon(Icons.Default.ChevronLeft, contentDescription = "Diminuir vencimento") }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Vencimento", style = MaterialTheme.typography.labelMedium)
                Button(onClick = { isCalendarOpen = true }) {
                    Text(dueDate.format(formatter))
                }
            }
            IconButton(onClick = onNextDueDay) { Icon(Icons.Default.ChevronRight, contentDescription = "Aumentar vencimento") }
        }
    }

    if (isCalendarOpen) {
        DatePickerDialog(
            onDismissRequest = { isCalendarOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis -> onDueDateSelected(millis.toLocalDate()) }
                        isCalendarOpen = false
                    }
                ) { Text("Selecionar") }
            },
            dismissButton = { TextButton(onClick = { isCalendarOpen = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SectionTitle(title: String, count: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        AssistChip(onClick = {}, label = { Text(count.toString()) })
    }
}

@Composable
private fun EmptyDebtsCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nenhuma dívida cadastrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Cadastre uma dívida acima para acompanhar vencimentos, status e progresso.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptySectionCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text = text, modifier = Modifier.fillMaxWidth().padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DebtCard(debt: Debt, onStartEdit: (Debt) -> Unit, onMarkAsPaid: (Debt) -> Unit, onDeleteDebt: (Debt) -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val paidProgress = if (debt.originalAmountInCents > 0L) {
        ((debt.originalAmountInCents - debt.remainingAmountInCents).toDouble() / debt.originalAmountInCents.toDouble()).toFloat().coerceIn(0f, 1f)
    } else 0f
    val paidPercent = (paidProgress * 100).toInt()

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = debt.creditor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = formatMoney(debt.remainingAmountInCents), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("$paidPercent% quitado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(progress = { paidProgress }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(debt.status.displayName) })
                    Text(text = "Vence em ${debt.dueDate.format(formatter)}", modifier = Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { onStartEdit(debt) }) { Icon(Icons.Default.Edit, contentDescription = "Editar dívida") }

            if (debt.status != DebtStatus.PAID) {
                IconButton(onClick = { onMarkAsPaid(debt) }) { Icon(Icons.Default.CheckCircle, contentDescription = "Marcar como paga") }
            }

            IconButton(onClick = { onDeleteDebt(debt) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir dívida") }
        }
    }
}

private fun LocalDate.toEpochMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun formatMoney(amountInCents: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return formatter.format(amountInCents / 100.0)
}
