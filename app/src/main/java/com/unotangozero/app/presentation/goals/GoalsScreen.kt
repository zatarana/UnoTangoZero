package com.unotangozero.app.presentation.goals

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun GoalsRoute(viewModel: GoalsViewModel = hiltViewModel()) {
    val goals by viewModel.goals.collectAsState()
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
        GoalsScreen(
            goals = goals,
            formState = formState,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onTargetValueChange = viewModel::onTargetValueChange,
            onDeadlineChange = viewModel::onDeadlineChange,
            onColorChange = viewModel::onColorChange,
            onStepTitleChange = viewModel::onStepTitleChange,
            onStepTypeChange = viewModel::onStepTypeChange,
            onAddStep = viewModel::addStep,
            onRemoveStep = viewModel::removeStep,
            onCreateGoal = viewModel::createGoal,
            onDeleteGoal = viewModel::deleteGoal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    goals: List<GoalUi>,
    formState: GoalFormUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onDeadlineChange: (LocalDate) -> Unit,
    onColorChange: (String) -> Unit,
    onStepTitleChange: (String) -> Unit,
    onStepTypeChange: (GoalStepType) -> Unit,
    onAddStep: () -> Unit,
    onRemoveStep: (String) -> Unit,
    onCreateGoal: () -> Unit,
    onDeleteGoal: (String) -> Unit
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
                    Text("Metas", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Crie metas SMART com valor alvo, prazo, passos, hábitos e tarefas vinculadas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item { GoalsSummaryCard(goals) }
            if (goals.isEmpty()) {
                item { EmptyGoalsCard() }
            } else {
                items(goals, key = { it.id }) { goal ->
                    GoalCard(goal = goal, onDeleteGoal = onDeleteGoal)
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
                formState = formState,
                onTitleChange = onTitleChange,
                onDescriptionChange = onDescriptionChange,
                onTargetValueChange = onTargetValueChange,
                onDeadlineChange = onDeadlineChange,
                onColorChange = onColorChange,
                onStepTitleChange = onStepTitleChange,
                onStepTypeChange = onStepTypeChange,
                onAddStep = onAddStep,
                onRemoveStep = onRemoveStep,
                onCreateGoal = {
                    onCreateGoal()
                    isFormOpen = false
                },
                onClose = { isFormOpen = false }
            )
        }
    }
}

@Composable
private fun GoalsSummaryCard(goals: List<GoalUi>) {
    val overdue = goals.count { it.deadline.isBefore(LocalDate.now()) }
    val nextDeadline = goals.minByOrNull { it.deadline }
    val totalSteps = goals.sumOf { it.steps.size }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("${goals.size} meta(s)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("$overdue atrasada(s) • $totalSteps passo(s) vinculados", color = MaterialTheme.colorScheme.onSurfaceVariant)
            nextDeadline?.let {
                Text("Próximo prazo: ${it.deadline.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun GoalCard(goal: GoalUi, onDeleteGoal: (String) -> Unit) {
    val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), goal.deadline)
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(goal.colorHex.toComposeColor()))
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (goal.description.isNotBlank()) {
                    Text(goal.description, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    goal.targetValueInCents?.let { AssistChip(onClick = {}, label = { Text("Alvo: ${money(it)}") }) }
                    AssistChip(onClick = {}, label = { Text("Prazo: ${goal.deadline.format(formatter)}") })
                    AssistChip(onClick = {}, label = { Text(if (daysLeft >= 0) "${daysLeft}d restantes" else "Atrasada") })
                }
                if (goal.steps.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(goal.steps, key = { it.id }) { step ->
                            AssistChip(onClick = {}, label = { Text("${step.type.displayName}: ${step.title}") })
                        }
                    }
                }
            }
            IconButton(onClick = { onDeleteGoal(goal.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir meta")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalFormSheet(
    formState: GoalFormUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onDeadlineChange: (LocalDate) -> Unit,
    onColorChange: (String) -> Unit,
    onStepTitleChange: (String) -> Unit,
    onStepTypeChange: (GoalStepType) -> Unit,
    onAddStep: () -> Unit,
    onRemoveStep: (String) -> Unit,
    onCreateGoal: () -> Unit,
    onClose: () -> Unit
) {
    val colorOptions = remember { listOf("#6750A4", "#B3261E", "#006A6A", "#386A20", "#8C5000", "#005DBA") }
    var isDateOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = formState.deadline.toEpochMillis())
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nova meta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text("Defina um objetivo e adicione passos que criam hábitos ou tarefas automaticamente.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = formState.title, onValueChange = onTitleChange, label = { Text("Título") }, placeholder = { Text("Ex: Passar em concurso") }, singleLine = true) }
        item { OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = formState.description, onValueChange = onDescriptionChange, label = { Text("Descrição") }, placeholder = { Text("Ex: Estudar até fechar o edital") }, minLines = 2) }
        item { OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = formState.targetValueText, onValueChange = onTargetValueChange, label = { Text("Valor alvo") }, prefix = { Text("R$ ") }, placeholder = { Text("Opcional") }, singleLine = true) }
        item {
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { isDateOpen = true }) {
                Text("Data limite: ${formState.deadline.format(formatter)}")
            }
        }
        item {
            Text("Cor", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(colorOptions) { colorHex ->
                    FilterChip(selected = formState.colorHex == colorHex, onClick = { onColorChange(colorHex) }, label = {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(14.dp).clip(CircleShape).background(colorHex.toComposeColor()))
                            Text(colorHex)
                        }
                    })
                }
            }
        }
        item { GoalStepsEditor(formState, onStepTitleChange, onStepTypeChange, onAddStep, onRemoveStep) }
        item { Button(modifier = Modifier.fillMaxWidth(), onClick = onCreateGoal) { Text("Criar meta") } }
        item { OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Cancelar") } }
    }

    if (isDateOpen) {
        DatePickerDialog(
            onDismissRequest = { isDateOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDeadlineChange(it.toLocalDate()) }
                    isDateOpen = false
                }) { Text("Selecionar") }
            },
            dismissButton = { TextButton(onClick = { isDateOpen = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun GoalStepsEditor(
    formState: GoalFormUiState,
    onStepTitleChange: (String) -> Unit,
    onStepTypeChange: (GoalStepType) -> Unit,
    onAddStep: () -> Unit,
    onRemoveStep: (String) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Passos vinculados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("Cada passo pode virar um hábito diário ou uma tarefa com prazo da meta.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = formState.stepTitle,
                onValueChange = onStepTitleChange,
                label = { Text("Nome do passo") },
                placeholder = { Text("Ex: Estudar 1h por dia") },
                singleLine = true
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(GoalStepType.entries) { type ->
                    FilterChip(
                        selected = formState.stepType == type,
                        onClick = { onStepTypeChange(type) },
                        label = { Text(type.displayName) }
                    )
                }
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onAddStep) { Text("Adicionar passo") }
            if (formState.steps.isEmpty()) {
                Text("Nenhum passo adicionado ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                formState.steps.forEach { step ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(step.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Será criado como ${step.type.displayName.lowercase()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onRemoveStep(step.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover passo")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGoalsCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nenhuma meta criada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Toque no + para criar uma meta com prazo, valor alvo, cor e passos vinculados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun money(cents: Long): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)

private fun String.toComposeColor(): Color = runCatching { Color(android.graphics.Color.parseColor(this)) }.getOrDefault(Color(0xFF6750A4))

private fun LocalDate.toEpochMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
