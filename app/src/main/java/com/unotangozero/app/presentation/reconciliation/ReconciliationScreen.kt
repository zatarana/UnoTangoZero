package com.unotangozero.app.presentation.reconciliation

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReconciliationScreen(
    balances: List<AccountBalance>,
    form: ReconciliationFormState,
    onAccountChange: (String?) -> Unit,
    onRealBalanceChange: (String) -> Unit,
    onReconcile: () -> Unit
) {
    var isSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Reconciliação", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Confira se o saldo do app bate com o saldo real.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { InfoCard() }
            item { Text("Saldos atuais", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) }

            if (balances.isEmpty()) {
                item { EmptyCard("Nenhuma conta encontrada. Cadastre uma conta antes de reconciliar.") }
            } else {
                items(balances, key = { it.account.id }) { balance ->
                    BalanceCard(balance)
                }
            }
        }

        if (balances.isNotEmpty()) {
            FloatingActionButton(
                onClick = { isSheetOpen = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Reconciliar saldo")
            }
        }
    }

    if (isSheetOpen && balances.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            ReconciliationFormSheet(
                balances = balances,
                form = form,
                onAccountChange = onAccountChange,
                onRealBalanceChange = onRealBalanceChange,
                onReconcile = {
                    onReconcile()
                    isSheetOpen = false
                },
                onClose = { isSheetOpen = false }
            )
        }
    }
}

@Composable
private fun InfoCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Como funciona", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("Informe o saldo real de uma conta. Se houver diferença, o app cria um ajuste para deixar tudo batendo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReconciliationFormSheet(
    balances: List<AccountBalance>,
    form: ReconciliationFormState,
    onAccountChange: (String?) -> Unit,
    onRealBalanceChange: (String) -> Unit,
    onReconcile: () -> Unit,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Reconciliar saldo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text("Escolha a conta e digite o saldo real visto no banco ou carteira.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Button(modifier = Modifier.fillMaxWidth(), onClick = onReconcile) { Text("Reconciliar saldo") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Cancelar") }
    }
}

@Composable
private fun BalanceCard(balance: AccountBalance) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(balance.account.name, fontWeight = FontWeight.ExtraBold)
                Text(balance.account.type.displayName, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(money(balance.currentBalanceInCents), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
