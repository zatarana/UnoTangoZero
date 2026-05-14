package com.unotangozero.app.presentation.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.enums.HabitFrequency
import com.unotangozero.app.domain.models.Habit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HabitsRoute(
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val habits by viewModel.habits.collectAsState()
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
        HabitsScreen(
            habits = habits,
            formState = formState,
            onNameChange = viewModel::onNameChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onTrackingTypeChange = viewModel::onTrackingTypeChange,
            onDailyGoalChange = viewModel::onDailyGoalChange,
            onScheduleTypeChange = viewModel::onScheduleTypeChange,
            onSelectedDayToggle = viewModel::onSelectedDayToggle,
            onIconChange = viewModel::onIconChange,
            onColorChange = viewModel::onColorChange,
            onReminderEnabledChange = viewModel::onReminderEnabledChange,
            onReminderTimeSelected = viewModel::onReminderTimeSelected,
            onCreateHabit = viewModel::createHabit,
            onCompleteToday = viewModel::completeToday,
            onDeleteHabit = viewModel::deleteHabit
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    habits: List<Habit>,
    formState: HabitFormUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTrackingTypeChange: (HabitTrackingType) -> Unit,
    onDailyGoalChange: (String) -> Unit,
    onScheduleTypeChange: (HabitScheduleType) -> Unit,
    onSelectedDayToggle: (DayOfWeek) -> Unit,
    onIconChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeSelected: (Int, Int) -> Unit,
    onCreateHabit: () -> Unit,
    onCompleteToday: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit
) {
    var isFormOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dailyHabits = remember(habits) { habits.filter { it.frequency == HabitFrequency.DAILY } }
    val weeklyHabits = remember(habits) { habits.filter { it.frequency == HabitFrequency.WEEKLY } }
    val monthlyHabits = remember(habits) { habits.filter { it.frequency == HabitFrequency.MONTHLY } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Hábitos", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Rastreie constância. Use tarefas recorrentes para compromissos com prazo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { HabitSummaryCard(habits = habits) }
            item { TodayHabitCard(habits = habits) }
            item { HabitGuidanceCard() }

            if (habits.isEmpty()) {
                item { EmptyHabitsCard() }
            } else {
                if (dailyHabits.isNotEmpty()) {
                    item { HabitSectionTitle("Diários", dailyHabits.size) }
                    items(items = dailyHabits, key = { it.id }) { habit ->
                        HabitCard(habit = habit, onCompleteToday = onCompleteToday, onDeleteHabit = onDeleteHabit)
                    }
                }
                if (weeklyHabits.isNotEmpty()) {
                    item { HabitSectionTitle("Semanais", weeklyHabits.size) }
                    items(items = weeklyHabits, key = { it.id }) { habit ->
                        HabitCard(habit = habit, onCompleteToday = onCompleteToday, onDeleteHabit = onDeleteHabit)
                    }
                }
                if (monthlyHabits.isNotEmpty()) {
                    item { HabitSectionTitle("Mensais", monthlyHabits.size) }
                    items(items = monthlyHabits, key = { it.id }) { habit ->
                        HabitCard(habit = habit, onCompleteToday = onCompleteToday, onDeleteHabit = onDeleteHabit)
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
            Icon(Icons.Default.Add, contentDescription = "Novo hábito")
        }
    }

    if (isFormOpen) {
        ModalBottomSheet(
            onDismissRequest = { isFormOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            HabitFormSheet(
                state = formState,
                onNameChange = onNameChange,
                onDescriptionChange = onDescriptionChange,
                onTrackingTypeChange = onTrackingTypeChange,
                onDailyGoalChange = onDailyGoalChange,
                onScheduleTypeChange = onScheduleTypeChange,
                onSelectedDayToggle = onSelectedDayToggle,
                onIconChange = onIconChange,
                onColorChange = onColorChange,
                onReminderEnabledChange = onReminderEnabledChange,
                onReminderTimeSelected = onReminderTimeSelected,
                onCreateHabit = {
                    onCreateHabit()
                    isFormOpen = false
                },
                onClose = { isFormOpen = false }
            )
        }
    }
}

@Composable
private fun HabitSectionTitle(title: String, count: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        AssistChip(onClick = {}, label = { Text(count.toString()) })
    }
}

@Composable
private fun HabitSummaryCard(habits: List<Habit>) {
    val daily = habits.count { it.frequency == HabitFrequency.DAILY }
    val weekly = habits.count { it.frequency == HabitFrequency.WEEKLY }
    val monthly = habits.count { it.frequency == HabitFrequency.MONTHLY }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${habits.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("hábitos ativos • $daily diário(s) • $weekly semanal(is) • $monthly mensal(is)", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TodayHabitCard(habits: List<Habit>) {
    val daily = habits.count { it.frequency == HabitFrequency.DAILY }
    val weekly = habits.count { it.frequency == HabitFrequency.WEEKLY }
    val today = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Hoje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(today, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$daily hábito(s) diário(s) para manter constância", fontWeight = FontWeight.Bold)
            Text("$weekly hábito(s) semanal(is) para acompanhar ao longo da semana", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Toque em Marcar hoje no card do hábito quando cumprir.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HabitGuidanceCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Como usar hábitos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("Hábito é rastreador de constância: beber água, caminhar, meditar, ler um pouco.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Tarefa recorrente é para algo com execução e prazo: pagar conta, entregar relatório, estudar um tópico específico.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitFormSheet(
    state: HabitFormUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTrackingTypeChange: (HabitTrackingType) -> Unit,
    onDailyGoalChange: (String) -> Unit,
    onScheduleTypeChange: (HabitScheduleType) -> Unit,
    onSelectedDayToggle: (DayOfWeek) -> Unit,
    onIconChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeSelected: (Int, Int) -> Unit,
    onCreateHabit: () -> Unit,
    onClose: () -> Unit
) {
    val iconOptions = remember { listOf("⭐", "💧", "🏃", "📚", "🧘", "💊", "💪", "📝") }
    val colorOptions = remember { listOf("#6750A4", "#B3261E", "#006A6A", "#386A20", "#8C5000", "#005DBA") }
    var isTimeOpen by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = state.reminderHour, initialMinute = state.reminderMinute, is24Hour = true)

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Novo hábito", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text("Crie hábitos flexíveis para marcar constância, quantidade, dias específicos e lembretes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.name,
            onValueChange = onNameChange,
            label = { Text("Nome do hábito") },
            placeholder = { Text("Ex: Caminhar, ler, beber água") },
            singleLine = true
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.description,
            onValueChange = onDescriptionChange,
            label = { Text("Descrição opcional") },
            placeholder = { Text("Ex: Meta leve, observação ou regra pessoal") },
            minLines = 2
        )
        ChipSelector("Tipo", HabitTrackingType.entries, state.trackingType, { it.displayName }, onTrackingTypeChange)
        if (state.trackingType == HabitTrackingType.NUMERIC) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.dailyGoalText,
                onValueChange = onDailyGoalChange,
                label = { Text("Meta diária") },
                placeholder = { Text("Ex: 8 copos, 20 páginas, 30 minutos") },
                singleLine = true
            )
        }
        ChipSelector("Frequência", HabitScheduleType.entries, state.scheduleType, { it.displayName }, onScheduleTypeChange)
        if (state.scheduleType == HabitScheduleType.SPECIFIC_DAYS) {
            Text("Dias específicos", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(weekDaysPtBr) { day ->
                    FilterChip(
                        selected = day in state.selectedDays,
                        onClick = { onSelectedDayToggle(day) },
                        label = { Text(day.shortPtBr()) }
                    )
                }
            }
        }
        Text("Ícone", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(iconOptions) { icon ->
                FilterChip(selected = state.icon == icon, onClick = { onIconChange(icon) }, label = { Text(icon) })
            }
        }
        Text("Cor", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(colorOptions) { colorHex ->
                FilterChip(
                    selected = state.colorHex == colorHex,
                    onClick = { onColorChange(colorHex) },
                    label = {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(14.dp).clip(CircleShape).background(colorHex.toComposeColor()))
                            Text(colorHex)
                        }
                    }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Lembrete", style = MaterialTheme.typography.labelLarge)
                Text(
                    if (state.reminderEnabled) "%02d:%02d".format(Locale("pt", "BR"), state.reminderHour, state.reminderMinute) else "Sem lembrete",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = state.reminderEnabled, onCheckedChange = onReminderEnabledChange)
        }
        if (state.reminderEnabled) {
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { isTimeOpen = true }) {
                Text("Escolher horário")
            }
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onCreateHabit) { Text("Criar hábito") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Cancelar") }
    }

    if (isTimeOpen) {
        AlertDialog(
            onDismissRequest = { isTimeOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    onReminderTimeSelected(timePickerState.hour, timePickerState.minute)
                    isTimeOpen = false
                }) { Text("Selecionar") }
            },
            dismissButton = { TextButton(onClick = { isTimeOpen = false }) { Text("Cancelar") } },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

@Composable
private fun <T> ChipSelector(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option ->
                FilterChip(selected = selected == option, onClick = { onSelect(option) }, label = { Text(label(option)) })
            }
        }
    }
}

@Composable
private fun EmptyHabitsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nenhum hábito ativo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Toque no + para criar um hábito de acompanhamento. Para compromissos com data e prazo, use Tarefas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    onCompleteToday: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                habit.description?.let {
                    Text(
                        text = it,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(onClick = {}, label = { Text(habit.frequency.displayName) })
                    Text(
                        text = "Criado em ${habit.createdAt.format(formatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = { onCompleteToday(habit) }) {
                    Text("Marcar hoje")
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { onDeleteHabit(habit) }) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir hábito")
            }
        }
    }
}

private val weekDaysPtBr = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

private fun DayOfWeek.shortPtBr(): String = when (this) {
    DayOfWeek.MONDAY -> "Seg"
    DayOfWeek.TUESDAY -> "Ter"
    DayOfWeek.WEDNESDAY -> "Qua"
    DayOfWeek.THURSDAY -> "Qui"
    DayOfWeek.FRIDAY -> "Sex"
    DayOfWeek.SATURDAY -> "Sáb"
    DayOfWeek.SUNDAY -> "Dom"
}

private fun String.toComposeColor(): Color {
    return runCatching { Color(android.graphics.Color.parseColor(this)) }.getOrDefault(Color(0xFF6750A4))
}
