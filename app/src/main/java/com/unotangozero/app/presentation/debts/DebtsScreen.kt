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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import java.time.LocalDate
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
    onSaveDebt: () -> Unit,
    onCancelEdit: () -> Unit,
    onStartEdit: (Debt) -> Unit,
    onMarkAsPaid: (Debt) -> Unit,
    onDeleteDebt: (Debt) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dívidas", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "Cadastre, edite e acompanhe dívidas sem cálculo de juros.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { DebtSummaryCard(summary = summary) }

        item {
            DebtEditorCard(
                state = editorState,
                onCreditorChange = onCreditorChange,
                onAmountChange = onAmountChange,
                onDescriptionChange = onDescriptionChange,
                onPreviousDueDay = onPreviousDueDay,
                onNextDueDay = onNextDueDay,
                onSaveDebt = onSaveDebt,
                onCancelEdit = onCancelEdit
            )
        }

        item {
            Text("Dívidas cadastradas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (debts.isEmpty()) {
            item { EmptyDebtsCard() }
        } else {
            items(items = debts, key = { it.id }) { debt ->
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

@Composable
private fun DebtEditorCard(
    state: DebtEditorUiState,
    onCreditorChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPreviousDueDay: () -> Unit,
    onNextDueDay: () -> Unit,
    onSaveDebt: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (state.isEditing) "Editar dívida" else "Nova dívida", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.creditor, onValueChange = onCreditorChange, label = { Text("Credor") }, singleLine = true)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.amountText, onValueChange = onAmountChange, label = { Text("Valor") }, singleLine = true, prefix = { Text("R$ ") })
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.description, onValueChange = onDescriptionChange, label = { Text("Descrição opcional") }, minLines = 2)
            DueDateSelector(dueDate = state.dueDate, onPreviousDueDay = onPreviousDueDay, onNextDueDay = onNextDueDay)
            Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveDebt) { Text(if (state.isEditing) "Salvar alterações" else "Cadastrar dívida") }
            if (state.isEditing) {
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onCancelEdit) { Text("Cancelar edição") }
            }
        }
    }
}

@Composable
private fun DebtSummaryCard(summary: DebtSummary) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo das dívidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(formatMoney(summary.totalDebtWithInterestInCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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

@Composable
private fun DueDateSelector(dueDate: LocalDate, onPreviousDueDay: () -> Unit, onNextDueDay: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousDueDay) { Icon(Icons.Default.ChevronLeft, contentDescription = "Diminuir vencimento") }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Vencimento", style = MaterialTheme.typography.labelMedium)
                Text(dueDate.format(formatter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onNextDueDay) { Icon(Icons.Default.ChevronRight, contentDescription = "Aumentar vencimento") }
        }
    }
}

@Composable
private fun EmptyDebtsCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nenhuma dívida cadastrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Cadastre uma dívida acima para acompanhar vencimentos.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DebtCard(debt: Debt, onStartEdit: (Debt) -> Unit, onMarkAsPaid: (Debt) -> Unit, onDeleteDebt: (Debt) -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = debt.creditor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = formatMoney(debt.totalDueInCents), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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

private fun formatMoney(amountInCents: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return formatter.format(amountInCents / 100.0)
}
