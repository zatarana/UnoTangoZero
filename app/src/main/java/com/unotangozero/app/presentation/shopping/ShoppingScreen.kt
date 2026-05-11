package com.unotangozero.app.presentation.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.ShoppingItem
import com.unotangozero.app.domain.models.ShoppingList
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ShoppingRoute(viewModel: ShoppingViewModel = hiltViewModel()) {
    val lists by viewModel.lists.collectAsState()
    val selectedListId by viewModel.selectedListId.collectAsState()
    val items by viewModel.items.collectAsState()
    val listName by viewModel.listName.collectAsState()
    val itemName by viewModel.itemName.collectAsState()
    val quantityText by viewModel.quantityText.collectAsState()
    val unit by viewModel.unit.collectAsState()
    val estimatedPriceText by viewModel.estimatedPriceText.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val currentMessage = message
        if (currentMessage != null) {
            snackbarHostState.showSnackbar(currentMessage)
            viewModel.clearMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)
        ShoppingScreen(
            lists = lists,
            selectedListId = selectedListId,
            items = items,
            listName = listName,
            itemName = itemName,
            quantityText = quantityText,
            unit = unit,
            estimatedPriceText = estimatedPriceText,
            onListNameChange = viewModel::onListNameChange,
            onCreateList = viewModel::createList,
            onSelectList = viewModel::selectList,
            onDeleteList = viewModel::deleteList,
            onItemNameChange = viewModel::onItemNameChange,
            onQuantityChange = viewModel::onQuantityChange,
            onUnitChange = viewModel::onUnitChange,
            onEstimatedPriceChange = viewModel::onEstimatedPriceChange,
            onAddItem = viewModel::addItem,
            onTogglePurchased = viewModel::togglePurchased,
            onDeleteItem = viewModel::deleteItem
        )
    }
}

@Composable
fun ShoppingScreen(
    lists: List<ShoppingList>,
    selectedListId: String?,
    items: List<ShoppingItem>,
    listName: String,
    itemName: String,
    quantityText: String,
    unit: String,
    estimatedPriceText: String,
    onListNameChange: (String) -> Unit,
    onCreateList: () -> Unit,
    onSelectList: (String) -> Unit,
    onDeleteList: (ShoppingList) -> Unit,
    onItemNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onEstimatedPriceChange: (String) -> Unit,
    onAddItem: () -> Unit,
    onTogglePurchased: (ShoppingItem) -> Unit,
    onDeleteItem: (ShoppingItem) -> Unit
) {
    val estimatedTotal = items.sumOf { ((it.estimatedPriceInCents ?: 0L) * it.quantity).toLong() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Compras", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Crie listas e acompanhe itens do mercado.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = listName, onValueChange = onListNameChange, label = { Text("Nome da lista") }, singleLine = true)
                    Button(modifier = Modifier.fillMaxWidth(), onClick = onCreateList) { Text("Criar lista") }
                }
            }
        }

        if (lists.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(lists, key = { it.id }) { list ->
                        FilterChip(selected = selectedListId == list.id, onClick = { onSelectList(list.id) }, label = { Text(list.name) })
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Estimativa", style = MaterialTheme.typography.labelLarge)
                    Text(formatMoney(estimatedTotal), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("${items.count { !it.isPurchased }} item(ns) pendente(s)", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = itemName, onValueChange = onItemNameChange, label = { Text("Item") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(modifier = Modifier.weight(1f), value = quantityText, onValueChange = onQuantityChange, label = { Text("Qtd") }, singleLine = true)
                        OutlinedTextField(modifier = Modifier.weight(1f), value = unit, onValueChange = onUnitChange, label = { Text("Un") }, singleLine = true)
                    }
                    OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = estimatedPriceText, onValueChange = onEstimatedPriceChange, label = { Text("Preço estimado") }, singleLine = true, prefix = { Text("R$ ") })
                    Button(modifier = Modifier.fillMaxWidth(), onClick = onAddItem) { Text("Adicionar item") }
                }
            }
        }

        item { Text("Itens", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

        if (selectedListId == null) {
            item { MessageCard("Selecione ou crie uma lista", "Crie uma lista acima para começar.") }
        } else if (items.isEmpty()) {
            item { MessageCard("Nenhum item", "Adicione itens à lista selecionada.") }
        } else {
            items(items, key = { it.id }) { item ->
                ShoppingItemCard(item = item, onTogglePurchased = onTogglePurchased, onDeleteItem = onDeleteItem)
            }
        }
    }
}

@Composable
private fun MessageCard(title: String, description: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ShoppingItemCard(item: ShoppingItem, onTogglePurchased: (ShoppingItem) -> Unit, onDeleteItem: (ShoppingItem) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = item.isPurchased, onCheckedChange = { onTogglePurchased(item) })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.itemName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null)
                Text("${item.quantity} ${item.unit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item.estimatedPriceInCents?.let { Text(formatMoney(it), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            IconButton(onClick = { onDeleteItem(item) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir item") }
        }
    }
}

private fun formatMoney(amountInCents: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return formatter.format(amountInCents / 100.0)
}
