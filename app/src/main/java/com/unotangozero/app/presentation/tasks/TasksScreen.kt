package com.unotangozero.app.presentation.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.enums.Priority
import com.unotangozero.app.domain.enums.RecurrenceType
import com.unotangozero.app.domain.enums.TaskCategory
import com.unotangozero.app.domain.models.Task
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TasksRoute(onOpenProjects: () -> Unit, viewModel: TasksViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val taskDurations by viewModel.taskDurations.collectAsState()
    val taskTags by viewModel.taskTags.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
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
        TasksScreen(
            tasks = tasks,
            taskDurations = taskDurations,
            taskTags = taskTags,
            selectedTag = selectedTag,
            editorState = editorState,
            onOpenProjects = onOpenProjects,
            onSmartTaskAdd = viewModel::addSmartTask,
            onTitleChange = viewModel::onTitleChange,
            onDueDatePreviousDay = viewModel::onDueDatePreviousDay,
            onDueDateNextDay = viewModel::onDueDateNextDay,
            onDueDateSelected = viewModel::onDueDateSelected,
            onDueDateToday = viewModel::onDueDateToday,
            onDueDateTomorrow = viewModel::onDueDateTomorrow,
            onDueDateNextWeek = viewModel::onDueDateNextWeek,
            onReminderTimeSelected = viewModel::onReminderTimeSelected,
            onCategoryChange = viewModel::onCategoryChange,
            onPriorityChange = viewModel::onPriorityChange,
            onRecurrenceTypeChange = viewModel::onRecurrenceTypeChange,
            onWeeklyDaySelected = viewModel::onWeeklyDaySelected,
            onMonthlyDaySelected = viewModel::onMonthlyDaySelected,
            onEstimatedHoursChange = viewModel::onEstimatedHoursChange,
            onEstimatedMinutesChange = viewModel::onEstimatedMinutesChange,
            onTagsChange = viewModel::onTagsChange,
            onSubtasksChange = viewModel::onSubtasksChange,
            onTagFilterChange = viewModel::onTagFilterChange,
            onSaveClick = viewModel::saveTaskFromEditor,
            onCancelEdit = viewModel::cancelEditing,
            onStartEdit = viewModel::startEditing,
            onToggleTask = viewModel::toggleCompleted,
            onDeleteTask = viewModel::deleteTask
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<Task>,
    taskDurations: Map<String, Int>,
    taskTags: Map<String, List<String>>,
    selectedTag: String?,
    editorState: TaskEditorUiState,
    onOpenProjects: () -> Unit,
    onSmartTaskAdd: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onDueDatePreviousDay: () -> Unit,
    onDueDateNextDay: () -> Unit,
    onDueDateSelected: (LocalDate) -> Unit,
    onDueDateToday: () -> Unit,
    onDueDateTomorrow: () -> Unit,
    onDueDateNextWeek: () -> Unit,
    onReminderTimeSelected: (Int, Int) -> Unit,
    onCategoryChange: (TaskCategory) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onRecurrenceTypeChange: (RecurrenceType) -> Unit,
    onWeeklyDaySelected: (DayOfWeek) -> Unit,
    onMonthlyDaySelected: (Int) -> Unit,
    onEstimatedHoursChange: (String) -> Unit,
    onEstimatedMinutesChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onSubtasksChange: (String) -> Unit,
    onTagFilterChange: (String?) -> Unit,
    onSaveClick: () -> Unit,
    onCancelEdit: () -> Unit,
    onStartEdit: (Task) -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    var isEditorOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(editorState.isEditing) { if (editorState.isEditing) isEditorOpen = true }

    val allTags = remember(taskTags) { taskTags.values.flatten().distinct().sorted() }
    val filteredTasks = remember(tasks, taskTags, selectedTag) {
        selectedTag?.let { tag -> tasks.filter { task -> taskTags[task.id].orEmpty().contains(tag) } } ?: tasks
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tarefas", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Digite naturalmente: pagar boleto amanhã às 14h p1.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item { SmartTaskInputCard(onSmartTaskAdd = onSmartTaskAdd) }
            item { TaskQuickActionsCard(onOpenProjects) }
            item { FocusSummaryCard(tasks = tasks) }
            if (allTags.isNotEmpty()) item { TagFilterRow(tags = allTags, selectedTag = selectedTag, onTagFilterChange = onTagFilterChange) }
            if (filteredTasks.isEmpty()) item { EmptyTasksCard(hasFilter = selectedTag != null) }
            else item { TaskCategoryKanbanBoard(filteredTasks, taskDurations, taskTags, onStartEdit, onToggleTask, onDeleteTask) }
        }

        FloatingActionButton(
            onClick = { isEditorOpen = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) { Icon(Icons.Default.Add, contentDescription = "Nova tarefa") }
    }

    if (isEditorOpen) {
        ModalBottomSheet(
            onDismissRequest = { onCancelEdit(); isEditorOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            TaskEditorSheet(
                state = editorState,
                onTitleChange = onTitleChange,
                onDueDatePreviousDay = onDueDatePreviousDay,
                onDueDateNextDay = onDueDateNextDay,
                onDueDateSelected = onDueDateSelected,
                onDueDateToday = onDueDateToday,
                onDueDateTomorrow = onDueDateTomorrow,
                onDueDateNextWeek = onDueDateNextWeek,
                onReminderTimeSelected = onReminderTimeSelected,
                onCategoryChange = onCategoryChange,
                onPriorityChange = onPriorityChange,
                onRecurrenceTypeChange = onRecurrenceTypeChange,
                onWeeklyDaySelected = onWeeklyDaySelected,
                onMonthlyDaySelected = onMonthlyDaySelected,
                onEstimatedHoursChange = onEstimatedHoursChange,
                onEstimatedMinutesChange = onEstimatedMinutesChange,
                onTagsChange = onTagsChange,
                onSubtasksChange = onSubtasksChange,
                onSaveClick = { onSaveClick(); isEditorOpen = false },
                onCancelEdit = { onCancelEdit(); isEditorOpen = false }
            )
        }
    }
}

@Composable
private fun SmartTaskInputCard(onSmartTaskAdd: (String) -> Unit) {
    var smartText by remember { mutableStateOf("") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = smartText,
                    onValueChange = { smartText = it },
                    label = { Text("Adicionar tarefa...") },
                    placeholder = { Text("Ex: estudar amanhã às 14h p1") },
                    singleLine = true
                )
                Button(
                    onClick = {
                        onSmartTaskAdd(smartText)
                        smartText = ""
                    }
                ) {
                    Text("Add")
                }
            }
            Text(
                "Reconhece hoje, amanhã, segunda, terça... horários como 14h/14:30 e prioridade p1, p2 ou p3.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TaskQuickActionsCard(onOpenProjects: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        LazyRow(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = false, onClick = onOpenProjects, label = { Text("Projetos") }) }
        }
    }
}

@Composable
private fun TaskCategoryKanbanBoard(tasks: List<Task>, taskDurations: Map<String, Int>, taskTags: Map<String, List<String>>, onStartEdit: (Task) -> Unit, onToggleTask: (Task) -> Unit, onDeleteTask: (Task) -> Unit) {
    val groupedTasks = remember(tasks) { tasks.groupBy { it.category }.toSortedMap(compareBy { it.displayName }) }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        groupedTasks.forEach { (category, categoryTasks) ->
            item(key = category.name) { TaskCategoryColumn(category, categoryTasks, taskDurations, taskTags, onStartEdit, onToggleTask, onDeleteTask) }
        }
    }
}

@Composable
private fun TaskCategoryColumn(category: TaskCategory, tasks: List<Task>, taskDurations: Map<String, Int>, taskTags: Map<String, List<String>>, onStartEdit: (Task) -> Unit, onToggleTask: (Task) -> Unit, onDeleteTask: (Task) -> Unit) {
    Card(modifier = Modifier.width(320.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${category.displayName} (${tasks.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            tasks.forEach { task -> TaskCard(task, taskDurations[task.id] ?: task.estimatedDurationMinutes, taskTags[task.id].orEmpty(), onStartEdit, onToggleTask, onDeleteTask) }
        }
    }
}

@Composable
private fun TagFilterRow(tags: List<String>, selectedTag: String?, onTagFilterChange: (String?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected = selectedTag == null, onClick = { onTagFilterChange(null) }, label = { Text("Todas") }) }
        items(tags) { tag -> FilterChip(selected = selectedTag == tag, onClick = { onTagFilterChange(tag) }, label = { Text("#$tag") }) }
    }
}

@Composable
private fun TaskEditorSheet(
    state: TaskEditorUiState,
    onTitleChange: (String) -> Unit,
    onDueDatePreviousDay: () -> Unit,
    onDueDateNextDay: () -> Unit,
    onDueDateSelected: (LocalDate) -> Unit,
    onDueDateToday: () -> Unit,
    onDueDateTomorrow: () -> Unit,
    onDueDateNextWeek: () -> Unit,
    onReminderTimeSelected: (Int, Int) -> Unit,
    onCategoryChange: (TaskCategory) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onRecurrenceTypeChange: (RecurrenceType) -> Unit,
    onWeeklyDaySelected: (DayOfWeek) -> Unit,
    onMonthlyDaySelected: (Int) -> Unit,
    onEstimatedHoursChange: (String) -> Unit,
    onEstimatedMinutesChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onSubtasksChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (state.isEditing) "Editar tarefa" else "Nova tarefa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.title, onValueChange = onTitleChange, label = { Text("Digite uma tarefa...") }, singleLine = true)
        DateSelector(state.dueDate, onDueDatePreviousDay, onDueDateNextDay, onDueDateSelected, onDueDateToday, onDueDateTomorrow, onDueDateNextWeek)
        TimeSelector(state.reminderHour, state.reminderMinute, onReminderTimeSelected)
        DurationFields(state, onEstimatedHoursChange, onEstimatedMinutesChange)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.tagsText, onValueChange = onTagsChange, label = { Text("Tags") }, placeholder = { Text("Ex: trabalho, casa, estudo") }, singleLine = true)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.subtasksText,
            onValueChange = onSubtasksChange,
            label = { Text("Subtarefas") },
            placeholder = { Text("Uma subtarefa por linha") },
            minLines = 3,
            maxLines = 6
        )
        ChipSelector("Categoria", TaskCategory.entries, state.category, { it.displayName }, onCategoryChange)
        ChipSelector("Prioridade", Priority.entries, state.priority, { it.displayName }, onPriorityChange)
        ChipSelector("Recorrência", listOf(RecurrenceType.NONE, RecurrenceType.DAILY, RecurrenceType.WEEKLY, RecurrenceType.MONTHLY, RecurrenceType.YEARLY), state.recurrenceType, { it.displayName }, onRecurrenceTypeChange)
        RecurrenceDetailsSelector(state, onWeeklyDaySelected, onMonthlyDaySelected)
        Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveClick) { Text(if (state.isEditing) "Salvar alterações" else "Adicionar") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onCancelEdit) { Text("Cancelar") }
    }
}

@Composable
private fun RecurrenceDetailsSelector(state: TaskEditorUiState, onWeeklyDaySelected: (DayOfWeek) -> Unit, onMonthlyDaySelected: (Int) -> Unit) {
    when (state.recurrenceType) {
        RecurrenceType.WEEKLY -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Dia da semana", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(weekDaysPtBr) { day ->
                    FilterChip(selected = state.dueDate.dayOfWeek == day, onClick = { onWeeklyDaySelected(day) }, label = { Text(day.shortPtBr()) })
                }
            }
        }
        RecurrenceType.MONTHLY -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Dia do mês", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items((1..31).toList()) { day ->
                    FilterChip(selected = state.dueDate.dayOfMonth == day, onClick = { onMonthlyDaySelected(day) }, label = { Text(day.toString()) })
                }
            }
            Text("Se o mês não tiver o dia escolhido, será usado o último dia válido.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        else -> Unit
    }
}

@Composable
private fun DurationFields(state: TaskEditorUiState, onEstimatedHoursChange: (String) -> Unit, onEstimatedMinutesChange: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(modifier = Modifier.weight(1f), value = state.estimatedHoursText, onValueChange = onEstimatedHoursChange, label = { Text("Horas") }, singleLine = true)
        OutlinedTextField(modifier = Modifier.weight(1f), value = state.estimatedMinutesText, onValueChange = onEstimatedMinutesChange, label = { Text("Min") }, singleLine = true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSelector(hour: Int, minute: Int, onTimeSelected: (Int, Int) -> Unit) {
    var isTimeOpen by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Horário do lembrete", style = MaterialTheme.typography.labelLarge)
        Button(onClick = { isTimeOpen = true }, modifier = Modifier.fillMaxWidth()) { Text("%02d:%02d".format(Locale("pt", "BR"), hour, minute)) }
    }
    if (isTimeOpen) {
        AlertDialog(
            onDismissRequest = { isTimeOpen = false },
            confirmButton = { TextButton(onClick = { onTimeSelected(timePickerState.hour, timePickerState.minute); isTimeOpen = false }) { Text("Selecionar") } },
            dismissButton = { TextButton(onClick = { isTimeOpen = false }) { Text("Cancelar") } },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelector(date: LocalDate, onPrevious: () -> Unit, onNext: () -> Unit, onDateSelected: (LocalDate) -> Unit, onToday: () -> Unit, onTomorrow: () -> Unit, onNextWeek: () -> Unit) {
    var isCalendarOpen by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.toEpochMillis())
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Data", style = MaterialTheme.typography.labelLarge)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious) { Icon(Icons.Default.ChevronLeft, contentDescription = "Dia anterior") }
                    Button(onClick = { isCalendarOpen = true }) { Text(date.format(formatter)) }
                    IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, contentDescription = "Próximo dia") }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = false, onClick = onToday, label = { Text("Hoje") }) }
                    item { FilterChip(selected = false, onClick = onTomorrow, label = { Text("Amanhã") }) }
                    item { FilterChip(selected = false, onClick = onNextWeek, label = { Text("Próx. semana") }) }
                }
            }
        }
    }
    if (isCalendarOpen) {
        DatePickerDialog(
            onDismissRequest = { isCalendarOpen = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { millis -> onDateSelected(millis.toLocalDate()) }; isCalendarOpen = false }) { Text("Selecionar") } },
            dismissButton = { TextButton(onClick = { isCalendarOpen = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun <T> ChipSelector(title: String, options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(options) { option -> FilterChip(selected = selected == option, onClick = { onSelect(option) }, label = { Text(label(option)) }) } }
    }
}

@Composable
private fun EmptyTasksCard(hasFilter: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (hasFilter) "Nenhuma tarefa com essa tag" else "Nenhuma tarefa cadastrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(if (hasFilter) "Limpe o filtro ou escolha outra tag." else "Toque no + para criar sua primeira tarefa.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TaskCard(task: Task, estimatedDurationMinutes: Int, tags: List<String>, onStartEdit: (Task) -> Unit, onToggleTask: (Task) -> Unit, onDeleteTask: (Task) -> Unit) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val subtasks = remember(task.description) { extractSubtasks(task.description) }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleTask(task) })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null)
                val recurrenceText = task.recurrenceType?.let { " • ${it.displayName}" } ?: ""
                val durationText = if (estimatedDurationMinutes > 0) " • ${formatDuration(estimatedDurationMinutes)}" else ""
                Text("${task.priority.displayName} • ${task.dueDate.format(dateFormatter)}$recurrenceText$durationText", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (subtasks.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        subtasks.take(3).forEach { subtask ->
                            Text("• $subtask", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (subtasks.size > 3) {
                            Text("+${subtasks.size - 3} subtarefas", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                if (tags.isNotEmpty()) LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(tags) { tag -> AssistChip(onClick = {}, label = { Text("#$tag") }) } }
                FocusTaskButton(task = task)
            }
            IconButton(onClick = { onStartEdit(task) }) { Icon(Icons.Default.Edit, contentDescription = "Editar tarefa") }
            IconButton(onClick = { onDeleteTask(task) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir tarefa") }
        }
    }
}

private val weekDaysPtBr = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
private const val SUBTASKS_MARKER = "[[SUBTAREFAS]]"

private fun DayOfWeek.shortPtBr(): String = when (this) {
    DayOfWeek.MONDAY -> "Seg"
    DayOfWeek.TUESDAY -> "Ter"
    DayOfWeek.WEDNESDAY -> "Qua"
    DayOfWeek.THURSDAY -> "Qui"
    DayOfWeek.FRIDAY -> "Sex"
    DayOfWeek.SATURDAY -> "Sáb"
    DayOfWeek.SUNDAY -> "Dom"
}

private fun extractSubtasks(description: String?): List<String> {
    if (description.isNullOrBlank()) return emptyList()
    val markerIndex = description.indexOf(SUBTASKS_MARKER)
    if (markerIndex < 0) return emptyList()
    return description.substring(markerIndex + SUBTASKS_MARKER.length)
        .lines()
        .map { it.trim().trimStart('-', '•').trim() }
        .filter { it.isNotBlank() }
}

private fun LocalDate.toEpochMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
private fun formatDuration(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
        hours > 0 -> "${hours}h"
        else -> "${minutes}min"
    }
}
