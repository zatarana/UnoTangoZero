package com.unotangozero.app.presentation.projection

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
    val mainHorizon = uiState.horizons.firstOrNull()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Projeção", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                Text("Veja como seus vencimentos afetam o saldo futuro.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item { ProjectionHeroCard(uiState.currentBalanceInCents, mainHorizon) }

        if (uiState.horizons.isNotEmpty()) {
            item { HorizonChips(uiState.horizons) }
            item { Text("Cenários", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) }
            items(uiState.horizons, key = { it.days }) { horizon -> ProjectionCard(horizon, uiState.currentBalanceInCents) }
        }

        item { Text("Contas consideradas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) }
        if (uiState.upcomingBills.isEmpty()) {
            item { EmptyProjectionCard("Nenhuma conta planejada aberta futura.") }
        } else {
            items(uiState.upcomingBills, key = { it.id }) { bill -> PlannedBillPreviewCard(bill) }
        }
    }
}

@Composable
private fun ProjectionHeroCard(currentBalanceInCents: Long, horizon: ProjectionHorizon?) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Saldo atual", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Text(money(currentBalanceInCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            if (horizon != null) {
                SummaryLine("Impacto em ${horizon.days} dias", horizon.plannedImpactInCents)
                SummaryLine("Saldo estimado", horizon.projectedBalanceInCents)
            }
        }
    }
}

@Composable
private fun HorizonChips(horizons: List<ProjectionHorizon>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(horizons, key = { it.days }) { horizon ->
            FilterChip(
                selected = false,
                onClick = {},
                label = { Text("${horizon.days} dias • ${money(horizon.projectedBalanceInCents)}") }
            )
        }
    }
}

@Composable
private fun ProjectionCard(horizon: ProjectionHorizon, currentBalanceInCents: Long) {
    val max = maxOf(abs(currentBalanceInCents), abs(horizon.projectedBalanceInCents), 1L).toFloat()
    val progress = (abs(horizon.projectedBalanceInCents).toFloat() / max).coerceIn(0f, 1f)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Próximos ${horizon.days} dias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            SummaryLine("Impacto planejado", horizon.plannedImpactInCents)
            SummaryLine("Saldo projetado", horizon.projectedBalanceInCents)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Text("${horizon.bills.size} conta(s) considerada(s)", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlannedBillPreviewCard(bill: PlannedBill) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(bill.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("${bill.type.displayName} • ${bill.dueDate.format(formatter)}${bill.category?.let { " • $it" } ?: ""}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(money(bill.amountInCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
private fun EmptyProjectionCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
