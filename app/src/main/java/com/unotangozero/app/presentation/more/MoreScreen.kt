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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
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
    onOpenSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Mais",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Acesse módulos extras do app sem lotar o menu principal.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            MoreCard(
                icon = Icons.Default.CreditCard,
                title = "Dívidas",
                description = "Controle dívidas, vencimentos e quitações.",
                onClick = onOpenDebts
            )
        }

        item {
            MoreCard(
                icon = Icons.Default.Repeat,
                title = "Hábitos",
                description = "Acompanhe hábitos e registre conclusões.",
                onClick = onOpenHabits
            )
        }

        item {
            MoreCard(
                icon = Icons.Default.ShoppingCart,
                title = "Compras",
                description = "Crie listas de compras e marque itens comprados.",
                onClick = onOpenShopping
            )
        }

        item {
            MoreCard(
                icon = Icons.Default.Notes,
                title = "Notas",
                description = "Guarde ideias, resumos e observações rápidas.",
                onClick = onOpenNotes
            )
        }

        item {
            MoreCard(
                icon = Icons.Default.Settings,
                title = "Configurações",
                description = "Ajuste notificações e horário padrão de lembrete.",
                onClick = onOpenSettings
            )
        }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
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
