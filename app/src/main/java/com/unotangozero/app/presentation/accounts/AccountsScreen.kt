package com.unotangozero.app.presentation.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.enums.FinancialAccountType
import com.unotangozero.app.domain.models.FinancialAccount
import com.unotangozero.app.domain.models.FinancialAccountsSummary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AccountsRoute(viewModel: AccountsViewModel = hiltViewModel()) {
    val accounts by viewModel.accounts.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val name by viewModel.name.collectAsState()
    val balanceText by viewModel.balanceText.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
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
        AccountsScreen(
            accounts = accounts.filter { !it.isArchived },
            summary = summary,
            name = name,
            balanceText = balanceText,
            selectedType = selectedType,
            onNameChange = viewModel::onNameChange,
            onBalanceChange = viewModel::onBalanceChange,
            onTypeChange = viewModel::onTypeChange,
            onSaveAccount = viewModel::saveAccount,
            onArchiveAccount = viewModel::archiveAccount
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    accounts: List<FinancialAccount>,
    summary: FinancialAccountsSummary,
    name: String,
    balanceText: String,
    selectedType: FinancialAccountType,
    onNameChange: (String) -> Unit,
    onBalanceChange: (String) -> Unit,
    onTypeChange: (FinancialAccountType) -> Unit,
    onSaveAccount: () -> Unit,
    onArchiveAccount: (FinancialAccount) -> Unit
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
                    Text("Contas", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Acompanhe onde seu dinheiro está.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { NetWorthCard(summary) }

            item { Text("Contas ativas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) }

            if (accounts.isEmpty()) {
                item { EmptyAccountsCard() }
            } else {
                items(accounts, key = { it.id }) { account ->
                    AccountCard(account, onArchiveAccount)
                }
            }
        }

        FloatingActionButton(
            onClick = { isFormOpen = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nova conta")
        }
    }

    if (isFormOpen) {
        ModalBottomSheet(
            onDismissRequest = { isFormOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            AccountFormSheet(
                name = name,
                balanceText = balanceText,
                selectedType = selectedType,
                onNameChange = onNameChange,
                onBalanceChange = onBalanceChange,
                onTypeChange = onTypeChange,
                onSaveAccount = {
                    onSaveAccount()
                    isFormOpen = false
                },
                onClose = { isFormOpen = false }
            )
        }
    }
}

@Composable
private fun NetWorthCard(summary: FinancialAccountsSummary) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Patrimônio líquido", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Text(money(summary.netWorthInCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            SummaryLine("Ativos", summary.totalAssetsInCents)
            if (summary.totalCreditCardDebtInCents != 0L) SummaryLine("Cartões", summary.totalCreditCardDebtInCents)
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(money(value), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AccountFormSheet(
    name: String,
    balanceText: String,
    selectedType: FinancialAccountType,
    onNameChange: (String) -> Unit,
    onBalanceChange: (String) -> Unit,
    onTypeChange: (FinancialAccountType) -> Unit,
    onSaveAccount: () -> Unit,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nova conta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nome da conta") },
            singleLine = true
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = balanceText,
            onValueChange = onBalanceChange,
            label = { Text("Saldo inicial real") },
            singleLine = true,
            prefix = { Text("R$ ") }
        )
        Text("Tipo", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FinancialAccountType.entries) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeChange(type) },
                    label = { Text(type.displayName) }
                )
            }
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveAccount) { Text("Criar conta") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Cancelar") }
    }
}

@Composable
private fun EmptyAccountsCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(
            text = "Nenhuma conta cadastrada. Toque no + para criar sua primeira conta.",
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AccountCard(account: FinancialAccount, onArchiveAccount: (FinancialAccount) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(account.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(money(account.initialBalanceInCents), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                AssistChip(onClick = {}, label = { Text(account.type.displayName) })
            }
            IconButton(onClick = { onArchiveAccount(account) }) {
                Icon(Icons.Default.Archive, contentDescription = "Arquivar")
            }
        }
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
