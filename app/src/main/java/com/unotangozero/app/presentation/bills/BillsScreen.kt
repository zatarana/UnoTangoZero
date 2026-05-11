package com.unotangozero.app.presentation.bills

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import com.unotangozero.app.domain.models.FinancialAccount
import com.unotangozero.app.domain.models.FinancialCategory
import com.unotangozero.app.domain.models.FinancialCategoryType
import com.unotangozero.app.domain.models.PlannedBill
import com.unotangozero.app.domain.models.PlannedBillType
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BillsRoute(viewModel: BillsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
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
        BillsScreen(
            uiState = uiState,
            categories = categories,
            form = form,
            onTypeChange = viewModel::onTypeChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onAmountChange = viewModel::onAmountChange,
            onCategoryChange = viewModel::onCategoryChange,
            onCategorySelected = viewModel::onCategorySelected,
            onAccountChange = viewModel::onAccountChange,
            onInstallmentsChange = viewModel::onInstallmentsChange,
            onPreviousDay = viewModel::previousDay,
            onNextDay = viewModel::nextDay,
            onSave = viewModel::saveBill,
            onMarkAsPaid = viewModel::markAsPaid,
            onDelete = viewModel::deleteBill
        )
    }
}

@Composable
fun BillsScreen(
    uiState: BillsUiState,
    categories: List<FinancialCategory>,
    form: BillFormState,
    onTypeChange: (PlannedBillType) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onCategorySelected: (FinancialCategory) -> Unit,
    onAccountChange: (String?) -> Unit,
    onInstallmentsChange: (String) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSave: () -> Unit,
    onMarkAsPaid: (PlannedBill) -> Unit,
    onDelete: (PlannedBill) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Contas planejadas", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Controle contas a pagar e a receber, gerando movimentações ao quitar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item { SummaryCard(uiState) }
        item {
            BillFormCard(
                accounts = uiState.accounts,
                categories = categories,
                form = form,
                onTypeChange = onTypeChange,
                onDescriptionChange = onDescriptionChange,
                onAmountChange = onAmountChange,
                onCategoryChange = onCategoryChange,
                onCategorySelected = onCategorySelected,
                onAccountChange = onAccountChange,
                onInstallmentsChange = onInstallmentsChange,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onSave = onSave
            )
        }

        item { Text("Lista", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (uiState.bills.isEmpty()) {
            item { Text("Nenhuma conta planejada cadastrada.") }
        } else {
            items(uiState.bills, key = { it.id }) { bill ->
                BillCard(bill = bill, accounts = uiState.accounts, onMarkAsPaid = onMarkAsPaid, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun SummaryCard(uiState: BillsUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Próximos vencimentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Próximos 7 dias")
                Text(money(uiState.dueNext7Days), fontWeight = FontWeight.SemiBold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Próximos 30 dias")
                Text(money(uiState.dueNext30Days), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BillFormCard(
    accounts: List<FinancialAccount>,
    categories: List<FinancialCategory>,
    form: BillFormState,
    onTypeChange: (PlannedBillType) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onCategorySelected: (FinancialCategory) -> Unit,
    onAccountChange: (String?) -> Unit,
    onInstallmentsChange: (String) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSave: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nova conta planejada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PlannedBillType.entries) { type ->
                    FilterChip(selected = form.type == type, onClick = { onTypeChange(type) }, label = { Text(type.displayName) })
                }
            }
            OutlinedTextField(Modifier.fillMaxWidth(), form.description, onDescriptionChange, label = { Text("Descrição") }, singleLine = true)
            OutlinedTextField(Modifier.fillMaxWidth(), form.amountText, onAmountChange, label = { Text("Valor da parcela") }, prefix = { Text("R$ ") }, singleLine = true)
            OutlinedTextField(Modifier.fillMaxWidth(), form.installmentsText, onInstallmentsChange, label = { Text("Quantidade de parcelas") }, singleLine = true)
            CategoryPicker(
                value = form.category,
                expectedType = if (form.type == PlannedBillType.PAYABLE) FinancialCategoryType.EXPENSE else FinancialCategoryType.INCOME,
                categories = categories,
                onCategoryChange = onCategoryChange,
                onCategorySelected = onCategorySelected
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousDay) { Icon(Icons.Default.ChevronLeft, null) }
                Text("Primeiro vencimento: ${form.dueDate.format(formatter)}", fontWeight = FontWeight.Bold)
                IconButton(onClick = onNextDay) { Icon(Icons.Default.ChevronRight, null) }
            }
            if (accounts.isNotEmpty()) {
                Text("Conta usada ao quitar/receber", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts, key = { it.id }) { account ->
                        FilterChip(selected = form.selectedAccountId == account.id, onClick = { onAccountChange(account.id) }, label = { Text(account.name) })
                    }
                }
            }
            Button(Modifier.fillMaxWidth(), onClick = onSave) { Text("Salvar") }
        }
    }
}

@Composable
private fun CategoryPicker(
    value: String,
    expectedType: FinancialCategoryType,
    categories: List<FinancialCategory>,
    onCategoryChange: (String) -> Unit,
    onCategorySelected: (FinancialCategory) -> Unit
) {
    val filtered = categories.filter { it.type == expectedType }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(Modifier.fillMaxWidth(), value, onCategoryChange, label = { Text("Categoria") }, singleLine = true)
        if (filtered.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { category ->
                    FilterChip(
                        selected = value == category.displayLabel,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.displayLabel) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BillCard(
    bill: PlannedBill,
    accounts: List<FinancialAccount>,
    onMarkAsPaid: (PlannedBill) -> Unit,
    onDelete: (PlannedBill) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val accountName = bill.accountId?.let { id -> accounts.firstOrNull { it.id == id }?.name }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(bill.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${bill.type.displayName} • ${bill.dueDate.format(formatter)} • ${money(bill.amountInCents)}")
                bill.category?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                if (bill.isPaid) {
                    Text("Quitado em ${bill.paidAt?.format(formatter) ?: "-"}${accountName?.let { " • $it" } ?: ""}")
                }
            }
            if (!bill.isPaid) {
                IconButton(onClick = { onMarkAsPaid(bill) }) { Icon(Icons.Default.CheckCircle, contentDescription = "Quitar") }
            }
            IconButton(onClick = { onDelete(bill) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
        }
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
