package com.unotangozero.app.presentation.projection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.PlannedBill
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun FutureBalanceProjectionRoute(viewModel: FutureBalanceProjectionViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    FutureBalanceProjectionScreen(uiState)
}

@Composable
fun FutureBalanceProjectionScreen(uiState: FutureBalanceProjectionUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Projeção de saldo", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Estimativa baseada no saldo atual e nas contas planejadas abertas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { CurrentBalanceCard(uiState.currentBalanceInCents) }
        items(uiState.horizons, key = { it.days }) { horizon -> ProjectionCard(horizon, uiState.currentBalanceInCents) }
        item { Text("Contas futuras consideradas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (uiState.upcomingBills.isEmpty()) {
            item { Text("Nenhuma conta planejada aberta futura.") }
        } else {
            items(uiState.upcomingBills, key = { it.id }) { bill -> PlannedBillPreviewCard(bill) }
        }
    }
}

@Composable
private fun CurrentBalanceCard(currentBalanceInCents: Long) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Saldo atual total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(money(currentBalanceInCents), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProjectionCard(horizon: ProjectionHorizon, currentBalanceInCents: Long) {
    val max = maxOf(abs(currentBalanceInCents), abs(horizon.projectedBalanceInCents), 1L).toFloat()
    val progress = (abs(horizon.projectedBalanceInCents).toFloat() / max).coerceIn(0f, 1f)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Próximos ${horizon.days} dias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Impacto planejado")
                Text(money(horizon.plannedImpactInCents), fontWeight = FontWeight.SemiBold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Saldo projetado")
                Text(money(horizon.projectedBalanceInCents), fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Text("${horizon.bills.size} conta(s) considerada(s)", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlannedBillPreviewCard(bill: PlannedBill) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(bill.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${bill.type.displayName} • ${bill.dueDate.format(formatter)}")
            Text(money(bill.amountInCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
