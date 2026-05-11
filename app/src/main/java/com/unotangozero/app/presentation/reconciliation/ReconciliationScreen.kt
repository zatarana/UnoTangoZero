package com.unotangozero.app.presentation.reconciliation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.AccountBalance
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReconciliationRoute(viewModel: ReconciliationViewModel = hiltViewModel()) {
    val balances by viewModel.balances.collectAsState()
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
        ReconciliationScreen(
            balances = balances,
            form = form,
            onAccountChange = viewModel::onAccountChange,
            onRealBalanceChange = viewModel::onRealBalanceChange,
            onReconcile = viewModel::reconcile
        )
    }
}

@Composable
fun ReconciliationScreen(
    balances: List<AccountBalance>,
    form: ReconciliationFormState,
    onAccountChange: (String?) -> Unit,
    onRealBalanceChange: (String) -> Unit,
    onReconcile: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Reconciliação", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Informe o saldo real da conta e gere um ajuste automático.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Conta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(balances, key = { it.account.id }) { balance ->
                            FilterChip(
                                selected = form.selectedAccountId == balance.account.id,
                                onClick = { onAccountChange(balance.account.id) },
                                label = { Text("${balance.account.name} • ${money(balance.currentBalanceInCents)}") }
                            )
                        }
                    }
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = form.realBalanceText,
                        onValueChange = onRealBalanceChange,
                        label = { Text("Saldo real") },
                        prefix = { Text("R$ ") },
                        singleLine = true
                    )
                    Button(modifier = Modifier.fillMaxWidth(), onClick = onReconcile) {
                        Text("Reconciliar saldo")
                    }
                }
            }
        }
        items(balances, key = { it.account.id }) { balance ->
            BalanceCard(balance)
        }
    }
}

@Composable
private fun BalanceCard(balance: AccountBalance) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(balance.account.name, fontWeight = FontWeight.Bold)
            Text(money(balance.currentBalanceInCents), fontWeight = FontWeight.Bold)
        }
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
