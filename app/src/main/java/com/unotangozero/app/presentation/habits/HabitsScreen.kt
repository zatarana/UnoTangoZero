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
import androidx.compose.foundation.layout.height
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
import com.unotangozero.app.domain.models.Habit
import com.unotangozero.app.domain.models.HabitFrequency
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

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
    val todayHabits = remember(habits) { habits.filter { it.isScheduledForToday() } }
    val otherHabits = remember(habits, todayHabits) { habits.filterNot { habit -> todayHabits.any { it.id == habit.id } } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Hábitos", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Lista do dia, sequências, XP, conquistas e estatísticas para manter constância.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { HabitGamificationCard(habits = habits) }
            item { HabitStatisticsCard(habits = habits) }
            item { HabitSummaryCard(habits = habits) }
            item { TodayHabitCard(todayHabits = todayHabits) }

            if (habits.isEmpty()) {
                item { EmptyHabitsCard() }
            } else {
                item { HabitSectionTitle("Programados para hoje", todayHabits.size) }
                if (todayHabits.isEmpty()) {
                    item { EmptyTodayHabitsCard() }
                } else {
                    items(items = todayHabits, key = { it.id }) { habit ->
                        TodayHabitItem(
                            habit = habit,
                            onCompleteToday = onCompleteToday,
                            onDeleteHabit = onDeleteHabit
                        )
                    }
                }

                if (otherHabits.isNotEmpty()) {
                    item { HabitSectionTitle("Outros hábitos", otherHabits.size) }
                    items(items = otherHabits, key = { it.id }) { habit ->
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
private fun HabitGamificationCard(habits: List<Habit>) {
    val gamification = remember(habits) { buildHabitGamification(habits) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(gamification.avatar, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Nível ${gamification.level}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("${gamification.totalXp} XP total", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LinearProgressIndicator(progress = { gamification.levelProgress }, modifier = Modifier.fillMaxWidth())
                    Text("${gamification.xpIntoLevel}/${gamification.xpForNextLevel} XP para o próximo nível", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("Conquistas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(gamification.badges, key = { it.title }) { badge ->
                    AssistChip(onClick = {}, label = { Text("${badge.icon} ${badge.title}") })
                }
            }
        }
    }
}

@Composable
private fun HabitStatisticsCard(habits: List<Habit>) {
    val stats = remember(habits) { buildHabitStatistics(habits) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Estatísticas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatisticMiniCard(modifier = Modifier.weight(1f), title = "Melhor sequência", value = "${stats.bestStreak}d")
                StatisticMiniCard(modifier = Modifier.weight(1f), title = "Total concluído", value = stats.totalCompleted.toString())
            }
            Text("Calendário anual", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            AnnualHeatMap(days = stats.heatMapDays)
            HeatMapLegend()
            Text("Histórico de conclusão", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            CompletionBarChart(points = stats.barPoints)
        }
    }
}

@Composable
private fun StatisticMiniCard(modifier: Modifier = Modifier, title: String, value: String) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun AnnualHeatMap(days: List<HeatMapDay>) {
    val leadingBlanks = days.firstOrNull()?.date?.dayOfWeek?.let { it.value % 7 } ?: 0
    val cells = List(leadingBlanks) { null } + days
    val weeks = cells.chunked(7)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        items(weeks) { week ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(7) { index ->
                    val day = week.getOrNull(index)
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(day?.let { heatMapColor(it.count) } ?: Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeatMapLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Menos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        listOf(0, 1, 2, 4).forEach { value ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(heatMapColor(value))
            )
        }
        Text("Mais", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CompletionBarChart(points: List<BarPoint>) {
    val maxValue = (points.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { point ->
            val progress = point.count / maxValue.toFloat()
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(point.count.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((22 + 96 * progress).dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(point.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
    val bestStreak = habits.maxOfOrNull { it.longestStreak } ?: 0
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${habits.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("hábitos ativos • $daily diário(s) • $weekly semanal(is) • $monthly mensal(is)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Maior recorde: $bestStreak dia(s)", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TodayHabitCard(todayHabits: List<Habit>) {
    val today = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }
    val bestTodayStreak = todayHabits.maxOfOrNull { it.currentStreak } ?: 0

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Hoje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(today, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${todayHabits.size} hábito(s) programado(s) para hoje", fontWeight = FontWeight.Bold)
            Text("Maior sequência ativa hoje: $bestTodayStreak dia(s)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Marque o checkbox grande quando cumprir. O streak é atualizado pelo registro do hábito.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.name, onValueChange = onNameChange, label = { Text("Nome do hábito") }, placeholder = { Text("Ex: Caminhar, ler, beber água") }, singleLine = true)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.description, onValueChange = onDescriptionChange, label = { Text("Descrição opcional") }, placeholder = { Text("Ex: Meta leve, observação ou regra pessoal") }, minLines = 2)
        ChipSelector("Tipo", HabitTrackingType.entries, state.trackingType, { it.displayName }, onTrackingTypeChange)
        if (state.trackingType == HabitTrackingType.NUMERIC) {
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.dailyGoalText, onValueChange = onDailyGoalChange, label = { Text("Meta diária") }, placeholder = { Text("Ex: 8 copos, 20 páginas, 30 minutos") }, singleLine = true)
        }
        ChipSelector("Frequência", HabitScheduleType.entries, state.scheduleType, { it.displayName }, onScheduleTypeChange)
        if (state.scheduleType == HabitScheduleType.SPECIFIC_DAYS) {
            Text("Dias específicos", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(weekDaysPtBr) { day ->
                    FilterChip(selected = day in state.selectedDays, onClick = { onSelectedDayToggle(day) }, label = { Text(day.shortPtBr()) })
                }
            }
        }
        Text("Ícone", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(iconOptions) { icon -> FilterChip(selected = state.icon == icon, onClick = { onIconChange(icon) }, label = { Text(icon) }) }
        }
        Text("Cor", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(colorOptions) { colorHex ->
                FilterChip(selected = state.colorHex == colorHex, onClick = { onColorChange(colorHex) }, label = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(14.dp).clip(CircleShape).background(colorHex.toComposeColor()))
                        Text(colorHex)
                    }
                })
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Lembrete", style = MaterialTheme.typography.labelLarge)
                Text(if (state.reminderEnabled) "%02d:%02d".format(Locale("pt", "BR"), state.reminderHour, state.reminderMinute) else "Sem lembrete", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = state.reminderEnabled, onCheckedChange = onReminderEnabledChange)
        }
        if (state.reminderEnabled) {
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { isTimeOpen = true }) { Text("Escolher horário") }
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onCreateHabit) { Text("Criar hábito") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Cancelar") }
    }

    if (isTimeOpen) {
        AlertDialog(onDismissRequest = { isTimeOpen = false }, confirmButton = {
            TextButton(onClick = { onReminderTimeSelected(timePickerState.hour, timePickerState.minute); isTimeOpen = false }) { Text("Selecionar") }
        }, dismissButton = { TextButton(onClick = { isTimeOpen = false }) { Text("Cancelar") } }, text = { TimePicker(state = timePickerState) })
    }
}

@Composable
private fun <T> ChipSelector(title: String, options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option -> FilterChip(selected = selected == option, onClick = { onSelect(option) }, label = { Text(label(option)) }) }
        }
    }
}

@Composable
private fun EmptyTodayHabitsCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nada programado para hoje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Você pode criar um hábito diário ou escolher dias específicos no botão +.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyHabitsCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nenhum hábito ativo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Toque no + para criar um hábito de acompanhamento. Para compromissos com data e prazo, use Tarefas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TodayHabitItem(habit: Habit, onCompleteToday: (Habit) -> Unit, onDeleteHabit: (Habit) -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    var checked by remember(habit.id, habit.lastCheckIn) { mutableStateOf(habit.lastCheckIn == LocalDate.now()) }
    var showConfetti by remember(habit.id) { mutableStateOf(false) }

    LaunchedEffect(showConfetti) {
        if (showConfetti) {
            delay(1_200)
            showConfetti = false
        }
    }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (showConfetti) {
                Text(text = "🎉 +10 XP ✨ 🎊", modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp), style = MaterialTheme.typography.titleLarge)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(modifier = Modifier.size(44.dp), checked = checked, onCheckedChange = { isChecked ->
                    if (isChecked && !checked) {
                        checked = true
                        showConfetti = true
                        onCompleteToday(habit)
                    }
                })
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(text = habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    habit.description.takeIf { it.isNotBlank() }?.let {
                        Text(text = it, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(onClick = {}, label = { Text(habit.frequency.name) })
                        AssistChip(onClick = {}, label = { Text("Sequência: ${habit.currentStreak} dia(s)") })
                        AssistChip(onClick = {}, label = { Text("Recorde: ${habit.longestStreak} dia(s)") })
                    }
                    Text(text = "Criado em ${habit.createdAt.format(formatter)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onDeleteHabit(habit) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir hábito")
                }
            }
        }
    }
}

@Composable
private fun HabitCard(habit: Habit, onCompleteToday: (Habit) -> Unit, onDeleteHabit: (Habit) -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                habit.description.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, maxLines = 3, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(onClick = {}, label = { Text(habit.frequency.name) })
                    AssistChip(onClick = {}, label = { Text("Sequência: ${habit.currentStreak}") })
                    AssistChip(onClick = {}, label = { Text("Recorde: ${habit.longestStreak}") })
                }
                Text(text = "Criado em ${habit.createdAt.format(formatter)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { onCompleteToday(habit) }) { Text("Marcar hoje") }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { onDeleteHabit(habit) }) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir hábito")
            }
        }
    }
}

private data class HabitGamificationUi(
    val avatar: String,
    val totalXp: Int,
    val level: Int,
    val xpIntoLevel: Int,
    val xpForNextLevel: Int,
    val levelProgress: Float,
    val badges: List<HabitBadgeUi>
)

private data class HabitBadgeUi(
    val icon: String,
    val title: String
)

private data class HabitStatisticsUi(
    val bestStreak: Int,
    val totalCompleted: Int,
    val heatMapDays: List<HeatMapDay>,
    val barPoints: List<BarPoint>
)

private data class HeatMapDay(
    val date: LocalDate,
    val count: Int
)

private data class BarPoint(
    val label: String,
    val count: Int
)

private fun buildHabitGamification(habits: List<Habit>): HabitGamificationUi {
    val completedCount = habits.sumOf { habit -> habit.checkIns.sumOf { it.completedTimes }.coerceAtLeast(if (habit.lastCheckIn != null) 1 else 0) }
    val streakBonus = habits.sumOf { it.currentStreak } * 2
    val recordBonus = habits.sumOf { it.longestStreak }
    val totalXp = completedCount * 10 + streakBonus + recordBonus
    val level = (totalXp / XP_PER_LEVEL) + 1
    val xpIntoLevel = totalXp % XP_PER_LEVEL
    val badges = buildList {
        if (completedCount >= 1) add(HabitBadgeUi("🌱", "Primeiro hábito"))
        if (completedCount >= 7) add(HabitBadgeUi("🔥", "Semana forte"))
        if (habits.any { it.currentStreak >= 7 }) add(HabitBadgeUi("⚡", "Streak 7"))
        if (habits.any { it.longestStreak >= 30 }) add(HabitBadgeUi("🏆", "Lenda 30"))
        if (habits.size >= 5) add(HabitBadgeUi("🧭", "Rotina ativa"))
        if (isEmpty()) add(HabitBadgeUi("🔒", "Complete hábitos"))
    }
    return HabitGamificationUi(
        avatar = if (level >= 10) "🏆" else "H",
        totalXp = totalXp,
        level = level,
        xpIntoLevel = xpIntoLevel,
        xpForNextLevel = XP_PER_LEVEL,
        levelProgress = xpIntoLevel / XP_PER_LEVEL.toFloat(),
        badges = badges
    )
}

private fun buildHabitStatistics(habits: List<Habit>): HabitStatisticsUi {
    val completionEvents = habits.flatMap { habit ->
        val checkInEvents = habit.checkIns.flatMap { checkIn -> List(checkIn.completedTimes.coerceAtLeast(1)) { checkIn.date } }
        if (checkInEvents.isEmpty() && habit.lastCheckIn != null) listOf(habit.lastCheckIn) else checkInEvents
    }
    val completionsByDate = completionEvents.groupingBy { it }.eachCount()
    val today = LocalDate.now()
    val yearStart = LocalDate.of(today.year, 1, 1)
    val heatMapDays = (0 until today.lengthOfYear()).map { offset ->
        val date = yearStart.plusDays(offset.toLong())
        HeatMapDay(date = date, count = completionsByDate[date] ?: 0)
    }
    val currentWeekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val barPoints = (7 downTo 0).map { weeksAgo ->
        val start = currentWeekStart.minusWeeks(weeksAgo.toLong())
        val end = start.plusDays(6)
        val count = completionsByDate.filterKeys { !it.isBefore(start) && !it.isAfter(end) }.values.sum()
        BarPoint(label = if (weeksAgo == 0) "Agora" else "-${weeksAgo}s", count = count)
    }
    return HabitStatisticsUi(
        bestStreak = habits.maxOfOrNull { it.longestStreak } ?: 0,
        totalCompleted = completionEvents.size,
        heatMapDays = heatMapDays,
        barPoints = barPoints
    )
}

private fun heatMapColor(count: Int): Color = when {
    count <= 0 -> Color(0xFFE6E0E9)
    count == 1 -> Color(0xFFB8E6C0)
    count == 2 -> Color(0xFF6FCF7F)
    count == 3 -> Color(0xFF2E7D32)
    else -> Color(0xFF14532D)
}

private const val XP_PER_LEVEL = 100

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

private fun Habit.isScheduledForToday(): Boolean {
    val today = LocalDate.now().dayOfWeek.shortPtBr()
    val descriptionText = description
    val hasSpecificDays = descriptionText.contains("Dias:", ignoreCase = true)
    return when (frequency) {
        HabitFrequency.DAILY -> true
        HabitFrequency.WEEKLY -> if (hasSpecificDays) descriptionText.contains(today, ignoreCase = true) else true
        HabitFrequency.MONTHLY -> LocalDate.now().dayOfMonth == createdAt.dayOfMonth
        HabitFrequency.CUSTOM -> descriptionText.contains(today, ignoreCase = true)
    }
}

private fun String.toComposeColor(): Color {
    return runCatching { Color(android.graphics.Color.parseColor(this)) }.getOrDefault(Color(0xFF6750A4))
}
