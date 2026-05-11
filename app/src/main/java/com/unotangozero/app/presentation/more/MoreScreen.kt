package com.unotangozero.app.presentation.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MoreRoute(
    onOpenDebts: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenShopping: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenProjects: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenKanban: () -> Unit,
    onOpenFocusMode: () -> Unit,
    onOpenMovements: () -> Unit,
    onOpenBills: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenProjection: () -> Unit,
    onOpenReconciliation: () -> Unit,
    onOpenCategories: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mais", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "Acesse módulos extras do app sem lotar o menu principal.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { MoreCard(Icons.Default.Folder, "Projetos", "Organize tarefas por objetivo e acompanhe progresso.", onOpenProjects) }
        item { MoreCard(Icons.Default.CenterFocusStrong, "Modo Foco", "Filtre tarefas por período, prioridade e tag.", onOpenFocusMode) }
        item { MoreCard(Icons.Default.ViewKanban, "Kanban", "Visualize tarefas por estágio e mova entre colunas.", onOpenKanban) }
        item { MoreCard(Icons.Default.Timer, "Foco", "Use Pomodoro, ciclos e registre esforço.", onOpenFocus) }
        item { MoreCard(Icons.Default.AccountBalanceWallet, "Contas", "Cadastre contas, carteiras, investimentos e cartões.", onOpenAccounts) }
        item { MoreCard(Icons.Default.Category, "Categorias financeiras", "Crie categorias e subcategorias de receitas e despesas.", onOpenCategories) }
        item { MoreCard(Icons.Default.Payments, "Movimentações", "Cadastre receitas, despesas e transferências entre contas.", onOpenMovements) }
        item { MoreCard(Icons.Default.Sync, "Reconciliação", "Ajuste o saldo do app para bater com o saldo real.", onOpenReconciliation) }
        item { MoreCard(Icons.Default.PieChart, "Orçamento", "Distribua receitas em envelopes mensais por categoria.", onOpenBudget) }
        item { MoreCard(Icons.Default.BarChart, "Relatórios financeiros", "Veja receitas, despesas e categorias por mês.", onOpenReports) }
        item { MoreCard(Icons.Default.BarChart, "Projeção de saldo", "Veja saldo projetado para 30, 60 e 90 dias.", onOpenProjection) }
        item { MoreCard(Icons.Default.EventNote, "Contas planejadas", "Controle contas a pagar e a receber.", onOpenBills) }
        item { MoreCard(Icons.Default.Flag, "Metas financeiras", "Acompanhe objetivos de economia e depósitos manuais.", onOpenGoals) }
        item { MoreCard(Icons.Default.CreditCard, "Dívidas", "Controle dívidas, vencimentos e quitações.", onOpenDebts) }
        item { MoreCard(Icons.Default.Repeat, "Hábitos", "Acompanhe hábitos e registre conclusões.", onOpenHabits) }
        item { MoreCard(Icons.Default.ShoppingCart, "Compras", "Crie listas de compras e marque itens comprados.", onOpenShopping) }
        item { MoreCard(Icons.Default.Notes, "Notas", "Guarde ideias, resumos e observações rápidas.", onOpenNotes) }
        item { MoreCard(Icons.Default.Settings, "Configurações", "Ajuste notificações e horário padrão de lembrete.", onOpenSettings) }
        item { MoreCard(Icons.Default.Backup, "Backup", "Crie e restaure cópias locais do banco de dados.", onOpenBackup) }
    }
}

@Composable
private fun MoreCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
