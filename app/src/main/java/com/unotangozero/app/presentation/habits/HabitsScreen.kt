package com.unotangozero.app.presentation.habits

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Hábitos",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Crie hábitos e registre conclusões diárias para acompanhar sua constância.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = name,
                        onValueChange = onNameChange,
                        label = { Text("Nome do hábito") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Descrição opcional") },
                        minLines = 2
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(HabitFrequency.entries) { item ->
                            FilterChip(
                                selected = frequency == item,
                                onClick = { onFrequencyChange(item) },
                                label = { Text(item.displayName) }
                            )
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onCreateHabit
                    ) {
                        Text("Criar hábito")
                    }
                }
            }
        }

        item {
            Text(
                text = "Hábitos ativos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (habits.isEmpty()) {
            item { EmptyHabitsCard() }
        } else {
            items(
                items = habits,
                key = { it.id }
            ) { habit ->
                HabitCard(
                    habit = habit,
                    onCompleteToday = onCompleteToday,
                    onDeleteHabit = onDeleteHabit
                )
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
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Nenhum hábito ativo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Crie um hábito acima para começar seu acompanhamento.", style = MaterialTheme.typography.bodyMedium)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                habit.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(habit.frequency.displayName) })
                    Text(
                        text = "Criado em ${habit.createdAt.format(formatter)}",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { onCompleteToday(habit) }) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Concluir hoje")
            }

            IconButton(onClick = { onDeleteHabit(habit) }) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir hábito")
            }
        }
    }
}
