package com.unotangozero.app.presentation.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.domain.enums.DebtStatus
import com.unotangozero.app.domain.models.Debt
import com.unotangozero.app.domain.models.DebtSummary
import com.unotangozero.app.domain.repositories.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FinanceDebtsPreviewViewModel @Inject constructor(
    debtRepository: DebtRepository
) : ViewModel() {
    val debts: StateFlow<List<Debt>> = debtRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summary: StateFlow<DebtSummary> = debtRepository.observeSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebtSummary())
}

@Composable
fun FinanceDebtsPreviewCard(
    onOpenDebts: () -> Unit,
    viewModel: FinanceDebtsPreviewViewModel = hiltViewModel()
) {
    val debts by viewModel.debts.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val totalRemainingInCents = debts
        .filter { it.status != DebtStatus.PAID }
        .sumOf { it.remainingAmountInCents }
        .coerceAtLeast(0L)

    Card(
        onClick = onOpenDebts,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Dívidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            if (summary.activeDebts == 0) {
                Text("Nenhuma dívida em aberto. Toque para cadastrar ou revisar histórico.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(money(totalRemainingInCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("em aberto", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ativas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(summary.activeDebts.toString(), fontWeight = FontWeight.Bold)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Quitadas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(summary.paidDebts.toString(), fontWeight = FontWeight.Bold)
                }
                summary.nextDueDate?.let { dueDate ->
                    Text(
                        text = "Próximo vencimento: ${dueDate.format(formatter)} • ${money(summary.nextDueAmountInCents)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}
