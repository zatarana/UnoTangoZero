package com.unotangozero.app.presentation.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Contas", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Cadastre saldos reais e acompanhe seu patrimônio líquido.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Patrimônio líquido", fontWeight = FontWeight.SemiBold)
                    Text(money(summary.netWorthInCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Ativos: ${money(summary.totalAssetsInCents)}")
                    Text("Cartões: ${money(summary.totalCreditCardDebtInCents)}")
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(FinancialAccountType.entries) { type ->
                            FilterChip(selected = selectedType == type, onClick = { onTypeChange(type) }, label = { Text(type.displayName) })
                        }
                    }
                    Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveAccount) { Text("Criar conta") }
                }
            }
        }

        item { Text("Contas ativas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

        if (accounts.isEmpty()) {
            item { Text("Nenhuma conta cadastrada ainda.") }
        } else {
            items(accounts, key = { it.id }) { account ->
                AccountCard(account, onArchiveAccount)
            }
        }
    }
}

@Composable
private fun AccountCard(account: FinancialAccount, onArchiveAccount: (FinancialAccount) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(account.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
