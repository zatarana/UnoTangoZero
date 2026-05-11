package com.unotangozero.app.presentation.categories

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
import androidx.compose.material.icons.filled.Archive
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
import com.unotangozero.app.domain.models.FinancialCategory
import com.unotangozero.app.domain.models.FinancialCategoryType

@Composable
fun FinancialCategoriesRoute(viewModel: FinancialCategoriesViewModel = hiltViewModel()) {
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
        FinancialCategoriesScreen(
            categories = categories,
            form = form,
            onNameChange = viewModel::onNameChange,
            onTypeChange = viewModel::onTypeChange,
            onParentNameChange = viewModel::onParentNameChange,
            onSave = viewModel::saveCategory,
            onArchive = viewModel::archiveCategory
        )
    }
}

@Composable
fun FinancialCategoriesScreen(
    categories: List<FinancialCategory>,
    form: FinancialCategoryFormState,
    onNameChange: (String) -> Unit,
    onTypeChange: (FinancialCategoryType) -> Unit,
    onParentNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onArchive: (FinancialCategory) -> Unit
) {
    val activeCategories = categories.filter { !it.isArchived }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Categorias financeiras", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Crie categorias e subcategorias para receitas e despesas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            CategoryFormCard(
                form = form,
                onNameChange = onNameChange,
                onTypeChange = onTypeChange,
                onParentNameChange = onParentNameChange,
                onSave = onSave
            )
        }
        item { Text("Categorias ativas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (activeCategories.isEmpty()) {
            item { Text("Nenhuma categoria cadastrada.") }
        } else {
            items(activeCategories, key = { it.id }) { category ->
                CategoryCard(category = category, onArchive = onArchive)
            }
        }
    }
}

@Composable
private fun CategoryFormCard(
    form: FinancialCategoryFormState,
    onNameChange: (String) -> Unit,
    onTypeChange: (FinancialCategoryType) -> Unit,
    onParentNameChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nova categoria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FinancialCategoryType.entries) { type ->
                    FilterChip(selected = form.type == type, onClick = { onTypeChange(type) }, label = { Text(type.displayName) })
                }
            }
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.name, onValueChange = onNameChange, label = { Text("Nome") }, singleLine = true)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = form.parentName, onValueChange = onParentNameChange, label = { Text("Categoria mãe opcional") }, placeholder = { Text("Ex: alimentação") }, singleLine = true)
            Button(modifier = Modifier.fillMaxWidth(), onClick = onSave) { Text("Salvar categoria") }
        }
    }
}

@Composable
private fun CategoryCard(category: FinancialCategory, onArchive: (FinancialCategory) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(category.displayLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                AssistChip(onClick = {}, label = { Text(category.type.displayName) })
            }
            IconButton(onClick = { onArchive(category) }) {
                Icon(Icons.Default.Archive, contentDescription = "Arquivar")
            }
        }
    }
}
