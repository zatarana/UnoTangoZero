package com.unotangozero.app.presentation.habits

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.enums.HabitFrequency
import com.unotangozero.app.domain.models.Habit
import java.time.format.DateTimeFormatter

@Composable
fun HabitsRoute(
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val habits by viewModel.habits.collectAsState()
    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val frequency by viewModel.frequency.collectAsState()
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
            name = name,
            description = description,
            frequency = frequency,
            onNameChange = viewModel::onNameChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onFrequencyChange = viewModel::onFrequencyChange,
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
    name: String,
    description: String,
    frequency: HabitFrequency,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onFrequencyChange: (HabitFrequency) -> Unit,
    onCreateHabit: () -> Unit,
    onCompleteToday: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit
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
                    Text("Hábitos", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Rastreie constância. Use tarefas recorrentes para compromissos com prazo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { HabitSummaryCard(habits = habits) }
            item { HabitGuidanceCard() }

            item {
                Text("Hábitos ativos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }

            if (habits.isEmpty()) {
                item { EmptyHabitsCard() }
            } else {
                items(items = habits, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        onCompleteToday = onCompleteToday,
                        onDeleteHabit = onDeleteHabit
                    )
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
                name = name,
                description = description,
                frequency = frequency,
                onNameChange = onNameChange,
                onDescriptionChange = onDescriptionChange,
                onFrequencyChange = onFrequencyChange,
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
private fun HabitGuidanceCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Como usar hábitos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("Hábito é rastreador de constância: beber água, caminhar, meditar, ler um pouco.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Tarefa recorrente é para algo com execução e prazo: pagar conta, entregar relatório, estudar um tópico específico.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HabitFormSheet(
    name: String,
    description: String,
    frequency: HabitFrequency,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onFrequencyChange: (HabitFrequency) -> Unit,
    onCreateHabit: () -> Unit,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Novo hábito", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text("Crie aqui hábitos que você quer marcar ao longo do tempo, sem transformar tudo em tarefa.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nome do hábito") },
            placeholder = { Text("Ex: Caminhar, ler, beber água") },
            singleLine = true
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Descrição opcional") },
            placeholder = { Text("Ex: Meta leve, observação ou regra pessoal") },
            minLines = 2
        )
        Text("Frequência", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(HabitFrequency.entries) { item ->
                FilterChip(
                    selected = frequency == item,
                    onClick = { onFrequencyChange(item) },
                    label = { Text(item.displayName) }
                )
            }
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onCreateHabit) { Text("Criar hábito") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Cancelar") }
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
                        maxLines = 2,
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
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { onCompleteToday(habit) }) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Marcar hábito hoje")
            }

            IconButton(onClick = { onDeleteHabit(habit) }) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir hábito")
            }
        }
    }
}
