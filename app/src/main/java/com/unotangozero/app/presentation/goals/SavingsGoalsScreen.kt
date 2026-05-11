package com.unotangozero.app.presentation.goals

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.SavingsGoal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.ceil

@Composable
fun SavingsGoalsRoute(viewModel: SavingsGoalsViewModel = hiltViewModel()) {
    val goals by viewModel.goals.collectAsState()
    val form by viewModel.form.collectAsState()
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
        SavingsGoalsScreen(
            goals = goals,
            form = form,
            onNameChange = viewModel::onNameChange,
            onTargetAmountChange = viewModel::onTargetAmountChange,
            onCategoryChange = viewModel::onCategoryChange,
            onHasTargetDateChange = viewModel::onHasTargetDateChange,
            onPreviousTargetDate = viewModel::previousTargetDate,
            onNextTargetDate = viewModel::nextTargetDate,
            onDepositAmountChange = viewModel::onDepositAmountChange,
            onDepositNoteChange = viewModel::onDepositNoteChange,
            onSaveGoal = viewModel::saveGoal,
            onAddDeposit = viewModel::addDeposit,
            onDeleteGoal = viewModel::deleteGoal
        )
    }
}

@Composable
fun SavingsGoalsScreen(
    goals: List<SavingsGoal>,
    form: SavingsGoalFormState,
    onNameChange: (String) -> Unit,
    onTargetAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onHasTargetDateChange: (Boolean) -> Unit,
    onPreviousTargetDate: () -> Unit,
    onNextTargetDate: () -> Unit,
    onDepositAmountChange: (String) -> Unit,
    onDepositNoteChange: (String) -> Unit,
    onSaveGoal: () -> Unit,
    onAddDeposit: (SavingsGoal) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Metas financeiras", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Crie metas, acompanhe progresso e registre depósitos manuais.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            GoalFormCard(
                form = form,
                onNameChange = onNameChange,
                onTargetAmountChange = onTargetAmountChange,
                onCategoryChange = onCategoryChange,
                onHasTargetDateChange = onHasTargetDateChange,
                onPreviousTargetDate = onPreviousTargetDate,
                onNextTargetDate = onNextTargetDate,
                onSaveGoal = onSaveGoal
            )
        }
        item { Text("Suas metas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (goals.isEmpty()) {
            item { Text("Nenhuma meta cadastrada.") }
        } else {
            items(goals, key = { it.id }) { goal ->
                GoalCard(
                    goal = goal,
                    form = form,
                    onDepositAmountChange = onDepositAmountChange,
                    onDepositNoteChange = onDepositNoteChange,
                    onAddDeposit = onAddDeposit,
                    onDeleteGoal = onDeleteGoal
                )
            }
        }
    }
}

@Composable
private fun GoalFormCard(
    form: SavingsGoalFormState,
    onNameChange: (String) -> Unit,
    onTargetAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onHasTargetDateChange: (Boolean) -> Unit,
    onPreviousTargetDate: () -> Unit,
    onNextTargetDate: () -> Unit,
    onSaveGoal: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nova meta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.name, onValueChange = onNameChange, label = { Text("Nome") }, singleLine = true)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.targetAmountText, onValueChange = onTargetAmountChange, label = { Text("Valor alvo") }, prefix = { Text("R$ ") }, singleLine = true)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.category, onValueChange = onCategoryChange, label = { Text("Categoria opcional") }, singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = form.hasTargetDate, onCheckedChange = onHasTargetDateChange)
                Text("Usar data desejada")
            }
            if (form.hasTargetDate) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPreviousTargetDate) { Icon(Icons.Default.ChevronLeft, null) }
                    Text(form.targetDate.format(formatter), fontWeight = FontWeight.Bold)
                    IconButton(onClick = onNextTargetDate) { Icon(Icons.Default.ChevronRight, null) }
                }
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveGoal) { Text("Criar meta") }
        }
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoal,
    form: SavingsGoalFormState,
    onDepositAmountChange: (String) -> Unit,
    onDepositNoteChange: (String) -> Unit,
    onAddDeposit: (SavingsGoal) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(goal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    goal.category?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                }
                IconButton(onClick = { onDeleteGoal(goal) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
            }
            LinearProgressIndicator(progress = { (goal.progressPercentage / 100.0).toFloat() }, modifier = Modifier.fillMaxWidth())
            Text("${money(goal.currentAmountInCents)} de ${money(goal.targetAmountInCents)} (${goal.progressPercentage.toInt()}%)")
            Text("Faltam ${money(goal.remainingAmountInCents)}")
            goal.targetDate?.let {
                Text("Data desejada: ${it.format(formatter)} • ${monthlyNeeded(goal)} por mês")
            }
            if (!goal.isCompleted) {
                OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.depositAmountText, onValueChange = onDepositAmountChange, label = { Text("Depósito") }, prefix = { Text("R$ ") }, singleLine = true)
                OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.depositNote, onValueChange = onDepositNoteChange, label = { Text("Observação") }, singleLine = true)
                Button(modifier = Modifier.fillMaxWidth(), onClick = { onAddDeposit(goal) }) { Text("Registrar depósito") }
            } else {
                Text("Meta concluída", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun monthlyNeeded(goal: SavingsGoal): String {
    val targetDate = goal.targetDate ?: return money(goal.remainingAmountInCents)
    val months = ChronoUnit.MONTHS.between(LocalDate.now().withDayOfMonth(1), targetDate.withDayOfMonth(1)).coerceAtLeast(1)
    val monthly = ceil(goal.remainingAmountInCents.toDouble() / months.toDouble()).toLong()
    return money(monthly)
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
