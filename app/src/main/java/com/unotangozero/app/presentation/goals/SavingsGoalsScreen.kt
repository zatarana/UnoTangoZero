package com.unotangozero.app.presentation.goals

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.unotangozero.app.domain.models.SavingsGoal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
            onTargetDateSelected = viewModel::onTargetDateSelected,
            onDepositAmountChange = viewModel::onDepositAmountChange,
            onDepositNoteChange = viewModel::onDepositNoteChange,
            onSaveGoal = viewModel::saveGoal,
            onAddDeposit = viewModel::addDeposit,
            onDeleteGoal = viewModel::deleteGoal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    onTargetDateSelected: (LocalDate) -> Unit,
    onDepositAmountChange: (String) -> Unit,
    onDepositNoteChange: (String) -> Unit,
    onSaveGoal: () -> Unit,
    onAddDeposit: (SavingsGoal) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit
) {
    var isFormOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activeGoals = remember(goals) { goals.filter { !it.isCompleted } }
    val completedGoals = remember(goals) { goals.filter { it.isCompleted } }
    val savedTotal = remember(goals) { goals.sumOf { it.currentAmountInCents } }
    val targetTotal = remember(goals) { goals.sumOf { it.targetAmountInCents } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Metas", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Organize objetivos financeiros agora e use Projetos/Tarefas para objetivos por etapas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { GoalsTypeOverviewCard() }
            item { GoalsSummaryCard(savedTotal = savedTotal, targetTotal = targetTotal, activeCount = activeGoals.size) }
            item { GoalsGuidanceCard() }

            if (activeGoals.isEmpty() && completedGoals.isEmpty()) {
                item { EmptyGoalsCard() }
            } else {
                if (activeGoals.isNotEmpty()) {
                    item { SectionTitle("Em andamento") }
                    items(activeGoals, key = { it.id }) { goal ->
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
                if (completedGoals.isNotEmpty()) {
                    item { SectionTitle("Concluídas") }
                    items(completedGoals, key = { it.id }) { goal ->
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

        FloatingActionButton(
            onClick = { isFormOpen = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nova meta")
        }
    }

    if (isFormOpen) {
        ModalBottomSheet(
            onDismissRequest = { isFormOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            GoalFormSheet(
                form = form,
                onNameChange = onNameChange,
                onTargetAmountChange = onTargetAmountChange,
                onCategoryChange = onCategoryChange,
                onHasTargetDateChange = onHasTargetDateChange,
                onPreviousTargetDate = onPreviousTargetDate,
                onNextTargetDate = onNextTargetDate,
                onTargetDateSelected = onTargetDateSelected,
                onSaveGoal = {
                    onSaveGoal()
                    isFormOpen = false
                },
                onClose = { isFormOpen = false }
            )
        }
    }
}

@Composable
private fun GoalsTypeOverviewCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Tipos de metas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Financeiras", fontWeight = FontWeight.ExtraBold)
                        Text("Disponível agora", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Valor, prazo, depósitos e progresso.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("De tarefas", fontWeight = FontWeight.ExtraBold)
                        Text("Usar Projetos", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Text("Etapas, checklist e Kanban por enquanto.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsSummaryCard(savedTotal: Long, targetTotal: Long, activeCount: Int) {
    val progress = if (targetTotal > 0L) (savedTotal.toDouble() / targetTotal.toDouble()).toFloat().coerceIn(0f, 1f) else 0f
    val progressPercent = (progress * 100).toInt()
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Progresso geral das metas financeiras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("$progressPercent%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("${money(savedTotal)} guardados de ${money(targetTotal)} • $activeCount ativa(s)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun GoalsGuidanceCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sobre esta aba", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("Use aqui para metas com valor: reserva de emergência, viagem, compra planejada ou quitação.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Metas baseadas em tarefas ainda devem ser organizadas em Projetos/Tarefas até virarem um módulo próprio.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
}

@Composable
private fun EmptyGoalsCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nenhuma meta financeira ainda", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Toque no + para criar uma meta com valor, prazo e progresso em porcentagem.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalFormSheet(
    form: SavingsGoalFormState,
    onNameChange: (String) -> Unit,
    onTargetAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onHasTargetDateChange: (Boolean) -> Unit,
    onPreviousTargetDate: () -> Unit,
    onNextTargetDate: () -> Unit,
    onTargetDateSelected: (LocalDate) -> Unit,
    onSaveGoal: () -> Unit,
    onClose: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    var isCalendarOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = form.targetDate.toEpochMillis())

    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nova meta financeira", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.name, onValueChange = onNameChange, label = { Text("Nome da meta") }, singleLine = true)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.targetAmountText, onValueChange = onTargetAmountChange, label = { Text("Valor alvo") }, prefix = { Text("R$ ") }, singleLine = true)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.category, onValueChange = onCategoryChange, label = { Text("Categoria opcional") }, singleLine = true)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = form.hasTargetDate, onCheckedChange = onHasTargetDateChange)
            Text("Usar data desejada")
        }
        if (form.hasTargetDate) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPreviousTargetDate) { Icon(Icons.Default.ChevronLeft, null) }
                        Button(onClick = { isCalendarOpen = true }) { Text(form.targetDate.format(formatter)) }
                        IconButton(onClick = onNextTargetDate) { Icon(Icons.Default.ChevronRight, null) }
                    }
                }
            }
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveGoal) { Text("Criar meta") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Cancelar") }
    }

    if (isCalendarOpen) {
        DatePickerDialog(
            onDismissRequest = { isCalendarOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis -> onTargetDateSelected(millis.toLocalDate()) }
                        isCalendarOpen = false
                    }
                ) { Text("Selecionar") }
            },
            dismissButton = {
                TextButton(onClick = { isCalendarOpen = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
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
    var isDepositOpen by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val progress = (goal.progressPercentage / 100.0).toFloat().coerceIn(0f, 1f)
    val progressPercent = goal.progressPercentage.toInt().coerceIn(0, 100)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(goal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    goal.category?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                }
                IconButton(onClick = { onDeleteGoal(goal) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
            }
            Text("$progressPercent% concluída", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Text("${money(goal.currentAmountInCents)} de ${money(goal.targetAmountInCents)}", fontWeight = FontWeight.Bold)
            Text("Faltam ${money(goal.remainingAmountInCents)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            goal.targetDate?.let { Text("${it.format(formatter)} • ${monthlyNeeded(goal)} por mês", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (!goal.isCompleted) {
                if (!isDepositOpen) {
                    Button(modifier = Modifier.fillMaxWidth(), onClick = { isDepositOpen = true }) { Text("Adicionar valor") }
                } else {
                    OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.depositAmountText, onValueChange = onDepositAmountChange, label = { Text("Valor") }, prefix = { Text("R$ ") }, singleLine = true)
                    OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.depositNote, onValueChange = onDepositNoteChange, label = { Text("Observação") }, singleLine = true)
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        onAddDeposit(goal)
                        isDepositOpen = false
                    }) { Text("Salvar") }
                    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { isDepositOpen = false }) { Text("Cancelar") }
                }
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

private fun LocalDate.toEpochMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
