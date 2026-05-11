package com.unotangozero.app.presentation.movements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.AccountBalance
import com.unotangozero.app.domain.models.FinancialAccount
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MovementsRoute(viewModel: MovementsViewModel = hiltViewModel()) {
    val accounts by viewModel.accounts.collectAsState()
    val balances by viewModel.balances.collectAsState()
    val movements by viewModel.movements.collectAsState()
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
        MovementsScreen(
            accounts = accounts.filter { !it.isArchived },
            balances = balances,
            movements = movements,
            form = form,
            onTypeChange = viewModel::onTypeChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onAmountChange = viewModel::onAmountChange,
            onCategoryChange = viewModel::onCategoryChange,
            onAccountChange = viewModel::onAccountChange,
            onFromAccountChange = viewModel::onFromAccountChange,
            onToAccountChange = viewModel::onToAccountChange,
            onPreviousDay = viewModel::previousDay,
            onNextDay = viewModel::nextDay,
            onSave = viewModel::save,
            onDelete = viewModel::deleteMovement
        )
    }
}

@Composable
fun MovementsScreen(
    accounts: List<FinancialAccount>,
    balances: List<AccountBalance>,
    movements: List<FinancialMovement>,
    form: MovementFormState,
    onTypeChange: (FinancialMovementType) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAccountChange: (String?) -> Unit,
    onFromAccountChange: (String?) -> Unit,
    onToAccountChange: (String?) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSave: () -> Unit,
    onDelete: (FinancialMovement) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Movimentações", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Cadastre receitas, despesas e transferências entre contas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (accounts.isEmpty()) {
            item { Text("Cadastre uma conta antes de criar movimentações.") }
        } else {
            item { BalancesCard(balances) }
            item {
                MovementFormCard(
                    accounts = accounts,
                    form = form,
                    onTypeChange = onTypeChange,
                    onDescriptionChange = onDescriptionChange,
                    onAmountChange = onAmountChange,
                    onCategoryChange = onCategoryChange,
                    onAccountChange = onAccountChange,
                    onFromAccountChange = onFromAccountChange,
                    onToAccountChange = onToAccountChange,
                    onPreviousDay = onPreviousDay,
                    onNextDay = onNextDay,
                    onSave = onSave
                )
            }
        }

        item { Text("Histórico", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (movements.isEmpty()) {
            item { Text("Nenhuma movimentação cadastrada.") }
        } else {
            items(movements, key = { it.id }) { movement -> MovementCard(movement, accounts, onDelete) }
        }
    }
}

@Composable
private fun BalancesCard(balances: List<AccountBalance>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Saldos atuais", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            balances.forEach { balance ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(balance.account.name)
                    Text(money(balance.currentBalanceInCents), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun MovementFormCard(
    accounts: List<FinancialAccount>,
    form: MovementFormState,
    onTypeChange: (FinancialMovementType) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAccountChange: (String?) -> Unit,
    onFromAccountChange: (String?) -> Unit,
    onToAccountChange: (String?) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSave: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nova movimentação", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FinancialMovementType.entries) { type ->
                    FilterChip(selected = form.type == type, onClick = { onTypeChange(type) }, label = { Text(type.displayName) })
                }
            }
            OutlinedTextField(Modifier.fillMaxWidth(), form.description, onDescriptionChange, label = { Text("Descrição") }, singleLine = true)
            OutlinedTextField(Modifier.fillMaxWidth(), form.amountText, onAmountChange, label = { Text("Valor") }, prefix = { Text("R$ ") }, singleLine = true)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousDay) { Icon(Icons.Default.ChevronLeft, null) }
                Text(form.date.format(formatter), fontWeight = FontWeight.Bold)
                IconButton(onClick = onNextDay) { Icon(Icons.Default.ChevronRight, null) }
            }

            when (form.type) {
                FinancialMovementType.INCOME -> {
                    OutlinedTextField(Modifier.fillMaxWidth(), form.category, onCategoryChange, label = { Text("Categoria da receita") }, singleLine = true)
                    AccountPicker("Conta de destino", accounts, form.accountId, onAccountChange)
                }
                FinancialMovementType.EXPENSE -> {
                    OutlinedTextField(Modifier.fillMaxWidth(), form.category, onCategoryChange, label = { Text("Categoria da despesa") }, singleLine = true)
                    AccountPicker("Conta de saída", accounts, form.accountId, onAccountChange)
                }
                FinancialMovementType.TRANSFER -> {
                    AccountPicker("Conta de origem", accounts, form.fromAccountId, onFromAccountChange)
                    AccountPicker("Conta de destino", accounts, form.toAccountId, onToAccountChange)
                }
            }

            Button(Modifier.fillMaxWidth(), onClick = onSave) { Text("Salvar movimentação") }
        }
    }
}

@Composable
private fun AccountPicker(title: String, accounts: List<FinancialAccount>, selectedId: String?, onSelect: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(accounts, key = { it.id }) { account ->
                FilterChip(selected = selectedId == account.id, onClick = { onSelect(account.id) }, label = { Text(account.name) })
            }
        }
    }
}

@Composable
private fun MovementCard(movement: FinancialMovement, accounts: List<FinancialAccount>, onDelete: (FinancialMovement) -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val accountName = when (movement.type) {
        FinancialMovementType.INCOME -> accounts.firstOrNull { it.id == movement.accountId }?.name ?: "Conta"
        FinancialMovementType.EXPENSE -> accounts.firstOrNull { it.id == movement.accountId }?.name ?: "Conta"
        FinancialMovementType.TRANSFER -> {
            val from = accounts.firstOrNull { it.id == movement.fromAccountId }?.name ?: "Origem"
            val to = accounts.firstOrNull { it.id == movement.toAccountId }?.name ?: "Destino"
            "$from → $to"
        }
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(movement.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${movement.type.displayName} • ${movement.date.format(formatter)} • $accountName")
                movement.category?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                Text(money(movement.amountInCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { onDelete(movement) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
        }
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
