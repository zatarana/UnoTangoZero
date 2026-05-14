package com.unotangozero.app.presentation.finance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.stickyHeader
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.FinancialAccount
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import com.unotangozero.app.domain.models.PlannedBill
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

@Composable
fun FinanceRoute(
    onOpenAccounts: () -> Unit,
    onOpenMovements: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenDebts: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenProjection: () -> Unit,
    onOpenReconciliation: () -> Unit,
    onOpenCategories: () -> Unit,
    viewModel: FinanceDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val currentMessage = message
        if (currentMessage != null) {
            snackbarHostState.showSnackbar(currentMessage)
            viewModel.clearMessage()
        }
    }

    FinanceScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onOpenAccounts = onOpenAccounts,
        onOpenMovements = onOpenMovements,
        onOpenBudget = onOpenBudget,
        onOpenGoals = onOpenGoals,
        onOpenDebts = onOpenDebts,
        onOpenReports = onOpenReports,
        onOpenProjection = onOpenProjection,
        onOpenReconciliation = onOpenReconciliation,
        onOpenCategories = onOpenCategories,
        onSaveQuickTransaction = viewModel::registerQuickTransaction,
        onDeleteMovement = viewModel::deleteMovement
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FinanceScreen(
    uiState: FinanceDashboardUiState,
    snackbarHostState: SnackbarHostState,
    onOpenAccounts: () -> Unit,
    onOpenMovements: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenDebts: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenProjection: () -> Unit,
    onOpenReconciliation: () -> Unit,
    onOpenCategories: () -> Unit,
    onSaveQuickTransaction: (String, FinancialMovementType, String, String?, LocalDate) -> Unit,
    onDeleteMovement: (String) -> Unit
) {
    var isQuickSheetOpen by remember { mutableStateOf(false) }
    var analysisMode by remember { mutableStateOf(AnalysisChartMode.PIE) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val currentMonthExpenses = remember(uiState.movements) { currentMonthExpenses(uiState.movements) }
    val categorySlices = remember(currentMonthExpenses) { expenseCategorySlices(currentMonthExpenses) }
    val timelinePoints = remember(currentMonthExpenses) { expenseTimelinePoints(currentMonthExpenses) }
    val filteredMovements = remember(uiState.movements, selectedCategory) {
        selectedCategory?.let { category ->
            uiState.movements.filter { movement -> movement.type == FinancialMovementType.EXPENSE && normalizedCategory(movement) == category }
        } ?: uiState.movements
    }
    val groupedMovements = remember(filteredMovements) { groupMovementsByDate(filteredMovements) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Finanças", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Resumo do seu dinheiro, orçamento, dívidas, lançamentos e projeções.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { BalanceHeroCard(uiState, onOpenAccounts) }
            item { FinanceQuickAccessRow(onOpenAccounts, onOpenBudget, onOpenReports, onOpenProjection, onOpenGoals, onOpenDebts, onOpenReconciliation, onOpenCategories) }
            item { MonthSummaryCard(uiState) }
            item { FinanceDebtsPreviewCard(onOpenDebts) }
            item {
                FinanceAnalysisCard(
                    mode = analysisMode,
                    selectedCategory = selectedCategory,
                    slices = categorySlices,
                    timelinePoints = timelinePoints,
                    onModeChange = { analysisMode = it },
                    onCategorySelected = { selectedCategory = if (selectedCategory == it) null else it },
                    onClearFilter = { selectedCategory = null }
                )
            }
            item { BudgetPreviewCard(uiState, onOpenBudget) }
            item { ProjectionPreviewCard(uiState, onOpenProjection) }

            item { SectionTitle(if (selectedCategory == null) "Transações" else "Transações • $selectedCategory") }
            if (groupedMovements.isEmpty()) {
                item { EmptyCard("Nenhum lançamento encontrado para o filtro atual.") }
            } else {
                groupedMovements.forEach { group ->
                    stickyHeader(key = "header-${group.title}") {
                        TransactionDateHeader(group.title)
                    }
                    items(group.movements, key = { it.id }) { movement ->
                        SwipeableMovementRow(
                            movement = movement,
                            accounts = uiState.accounts,
                            onDeleteMovement = onDeleteMovement
                        )
                    }
                }
            }

            if (uiState.overdueBills.isNotEmpty()) {
                item { SectionTitle("Vencidas") }
                items(uiState.overdueBills, key = { it.id }) { bill -> BillPreviewCard(bill) }
            }

            item { SectionTitle("Próximos 30 dias") }
            if (uiState.upcomingBills.isEmpty()) {
                item { EmptyCard("Nenhum vencimento previsto para os próximos 30 dias.") }
            } else {
                items(uiState.upcomingBills, key = { it.id }) { bill -> BillPreviewCard(bill) }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp))

        FloatingActionButton(
            onClick = { isQuickSheetOpen = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Novo lançamento")
        }
    }

    if (isQuickSheetOpen) {
        QuickTransactionSheet(
            uiState = uiState,
            onDismiss = { isQuickSheetOpen = false },
            onSave = { amountText, type, category, accountId, date ->
                onSaveQuickTransaction(amountText, type, category, accountId, date)
                isQuickSheetOpen = false
            }
        )
    }
}

@Composable
private fun FinanceAnalysisCard(
    mode: AnalysisChartMode,
    selectedCategory: String?,
    slices: List<CategoryExpenseSlice>,
    timelinePoints: List<TimelineExpensePoint>,
    onModeChange: (AnalysisChartMode) -> Unit,
    onCategorySelected: (String) -> Unit,
    onClearFilter: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Análise do mês", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text("Toque em uma categoria para filtrar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = mode == AnalysisChartMode.PIE, onClick = { onModeChange(AnalysisChartMode.PIE) }, label = { Text("Pizza") })
                    FilterChip(selected = mode == AnalysisChartMode.TIMELINE, onClick = { onModeChange(AnalysisChartMode.TIMELINE) }, label = { Text("Linha") })
                }
            }
            if (selectedCategory != null) {
                FilterChip(selected = true, onClick = onClearFilter, label = { Text("Filtro: $selectedCategory ✕") })
            }
            if (slices.isEmpty()) {
                Text("Nenhuma despesa registrada neste mês.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (mode == AnalysisChartMode.PIE) {
                ExpensePieAnalysis(slices = slices, selectedCategory = selectedCategory, onCategorySelected = onCategorySelected)
            } else {
                ExpenseTimelineAnalysis(points = timelinePoints)
                CategoryFilterRow(slices = slices, selectedCategory = selectedCategory, onCategorySelected = onCategorySelected)
            }
        }
    }
}

@Composable
private fun ExpensePieAnalysis(
    slices: List<CategoryExpenseSlice>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    val total = slices.sumOf { it.amountInCents }.toFloat().coerceAtLeast(1f)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val strokeWidth = size.minDimension * 0.22f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            var startAngle = -90f
            slices.forEachIndexed { index, slice ->
                val sweepAngle = (slice.amountInCents / total) * 360f
                drawArc(
                    color = chartColor(index),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += sweepAngle
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slices.take(6).forEachIndexed { index, slice ->
                CategoryLegendRow(
                    slice = slice,
                    color = chartColor(index),
                    totalInCents = slices.sumOf { it.amountInCents },
                    selected = selectedCategory == slice.category,
                    onClick = { onCategorySelected(slice.category) }
                )
            }
        }
    }
}

@Composable
private fun ExpenseTimelineAnalysis(points: List<TimelineExpensePoint>) {
    val maxAmount = max(1L, points.maxOfOrNull { it.amountInCents } ?: 1L).toFloat()
    Canvas(modifier = Modifier.fillMaxWidth().height(170.dp)) {
        if (points.isEmpty()) return@Canvas
        val horizontalGap = if (points.size == 1) size.width else size.width / (points.size - 1)
        val mappedPoints = points.mapIndexed { index, point ->
            val x = if (points.size == 1) size.width / 2f else horizontalGap * index
            val y = size.height - ((point.amountInCents / maxAmount) * size.height)
            Offset(x, y.coerceIn(0f, size.height))
        }
        mappedPoints.zipWithNext().forEach { (start, end) ->
            drawLine(
                color = Color(0xFF6750A4),
                start = start,
                end = end,
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )
        }
        mappedPoints.forEach { point ->
            drawCircle(color = Color(0xFF6750A4), radius = 7f, center = point)
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Início", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Hoje", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CategoryFilterRow(
    slices: List<CategoryExpenseSlice>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(slices, key = { it.category }) { slice ->
            FilterChip(
                selected = selectedCategory == slice.category,
                onClick = { onCategorySelected(slice.category) },
                label = { Text(slice.category) }
            )
        }
    }
}

@Composable
private fun CategoryLegendRow(
    slice: CategoryExpenseSlice,
    color: Color,
    totalInCents: Long,
    selected: Boolean,
    onClick: () -> Unit
) {
    val percent = if (totalInCents > 0L) ((slice.amountInCents * 100) / totalInCents).toInt() else 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Column(modifier = Modifier.weight(1f)) {
            Text(slice.category, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text("$percent% • ${money(slice.amountInCents)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickTransactionSheet(
    uiState: FinanceDashboardUiState,
    onDismiss: () -> Unit,
    onSave: (String, FinancialMovementType, String, String?, LocalDate) -> Unit
) {
    val categories = remember { listOf("Alimentação", "Transporte", "Moradia", "Saúde", "Educação", "Lazer", "Salário", "Dívidas", "Outros") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(FinancialMovementType.EXPENSE) }
    var category by remember { mutableStateOf(categories.first()) }
    var accountId by remember(uiState.accounts) { mutableStateOf(uiState.accounts.firstOrNull()?.id) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.toEpochMillis())
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val selectedAccountName = uiState.accounts.firstOrNull { it.id == accountId }?.name ?: "Selecione uma conta"

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Novo lançamento rápido", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = amountText,
                onValueChange = { amountText = it.filterMoneyChars() },
                label = { Text("Valor") },
                prefix = { Text("R$ ") },
                singleLine = true
            )
            QuickDropdown(
                label = "Tipo",
                selectedText = type.displayName,
                options = listOf(FinancialMovementType.EXPENSE.displayName, FinancialMovementType.INCOME.displayName),
                onOptionSelected = { selected ->
                    type = if (selected == FinancialMovementType.INCOME.displayName) FinancialMovementType.INCOME else FinancialMovementType.EXPENSE
                }
            )
            QuickDropdown(
                label = "Categoria",
                selectedText = category,
                options = categories,
                onOptionSelected = { category = it }
            )
            QuickDropdown(
                label = "Conta",
                selectedText = selectedAccountName,
                options = uiState.accounts.map { it.name },
                onOptionSelected = { selectedName ->
                    accountId = uiState.accounts.firstOrNull { it.name == selectedName }?.id
                }
            )
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { isDatePickerOpen = true }) {
                Text("Data: ${date.format(formatter)}")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSave(amountText, type, category, accountId, date) }
            ) {
                Text("Salvar lançamento")
            }
        }
    }

    if (isDatePickerOpen) {
        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { date = it.toLocalDate() }
                        isDatePickerOpen = false
                    }
                ) { Text("Selecionar") }
            },
            dismissButton = { TextButton(onClick = { isDatePickerOpen = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun QuickDropdown(
    label: String,
    selectedText: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { expanded = true }) {
            Text("$label: $selectedText")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("Nenhuma opção disponível") }, onClick = { expanded = false })
            } else {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FinanceQuickAccessRow(
    onOpenAccounts: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenProjection: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenDebts: () -> Unit,
    onOpenReconciliation: () -> Unit,
    onOpenCategories: () -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected = false, onClick = onOpenAccounts, label = { Text("Contas") }) }
        item { FilterChip(selected = false, onClick = onOpenBudget, label = { Text("Orçamento") }) }
        item { FilterChip(selected = false, onClick = onOpenReports, label = { Text("Relatórios") }) }
        item { FilterChip(selected = false, onClick = onOpenProjection, label = { Text("Projeção") }) }
        item { FilterChip(selected = false, onClick = onOpenGoals, label = { Text("Metas") }) }
        item { FilterChip(selected = false, onClick = onOpenDebts, label = { Text("Dívidas") }) }
        item { FilterChip(selected = false, onClick = onOpenReconciliation, label = { Text("Reconciliar") }) }
        item { FilterChip(selected = false, onClick = onOpenCategories, label = { Text("Categorias") }) }
    }
}

@Composable
private fun BalanceHeroCard(uiState: FinanceDashboardUiState, onOpenAccounts: () -> Unit) {
    Card(onClick = onOpenAccounts, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Saldo total", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(money(uiState.totalBalanceInCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("${uiState.accountCount} conta(s) cadastrada(s) • toque para gerenciar", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MonthSummaryCard(uiState: FinanceDashboardUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Este mês", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniMoneyCard(modifier = Modifier.weight(1f), title = "Receitas", value = uiState.monthlyIncomeInCents)
                MiniMoneyCard(modifier = Modifier.weight(1f), title = "Despesas", value = uiState.monthlyExpenseInCents)
            }
            SummaryLine("Resultado", uiState.monthlyBalanceInCents)
            SummaryLine("Lançamentos", uiState.monthlyMovementCount.toLong(), isMoney = false)
            if (uiState.monthlyAdjustmentInCents != 0L) SummaryLine("Ajustes", uiState.monthlyAdjustmentInCents)
        }
    }
}

@Composable
private fun MiniMoneyCard(modifier: Modifier = Modifier, title: String, value: Long) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(money(value), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableMovementRow(
    movement: FinancialMovement,
    accounts: List<FinancialAccount>,
    onDeleteMovement: (String) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                onDeleteMovement(movement.id)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.onErrorContainer)
                    Text("Excluir", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) {
        MovementListRow(movement = movement, accounts = accounts)
    }
}

@Composable
private fun MovementListRow(movement: FinancialMovement, accounts: List<FinancialAccount>) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM") }
    val signedAmount = when (movement.type) {
        FinancialMovementType.EXPENSE -> -movement.amountInCents
        else -> movement.amountInCents
    }
    val accountName = resolveAccountName(movement, accounts)

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon(movement), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(movement.description, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${movement.category ?: movement.type.displayName} • $accountName • ${movement.date.format(formatter)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(money(signedAmount), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun TransactionDateHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 6.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BudgetPreviewCard(uiState: FinanceDashboardUiState, onOpenBudget: () -> Unit) {
    val budget = uiState.budgetSummary
    Card(onClick = onOpenBudget, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Orçamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            if (budget == null || budget.envelopes.isEmpty()) {
                Text("Nenhum envelope criado para este mês. Toque para começar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                SummaryLine("Disponível", budget.totalAvailableInCents)
                SummaryLine("Gasto", budget.totalSpentInCents)
                SummaryLine("A distribuir", budget.amountToDistributeInCents)
                val progress = if (budget.totalAvailableInCents > 0L) {
                    (budget.totalSpentInCents.toDouble() / budget.totalAvailableInCents.toDouble()).toFloat().coerceIn(0f, 1f)
                } else 0f
                Text("${(progress * 100).toInt()}% usado", fontWeight = FontWeight.Bold)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ProjectionPreviewCard(uiState: FinanceDashboardUiState, onOpenProjection: () -> Unit) {
    Card(onClick = onOpenProjection, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Projeção", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            SummaryLine("Impacto em 30 dias", uiState.next30DaysImpactInCents)
            SummaryLine("Saldo estimado", uiState.totalBalanceInCents + uiState.next30DaysImpactInCents)
        }
    }
}

@Composable
private fun BillPreviewCard(bill: PlannedBill) {
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
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
}

@Composable
private fun EmptyCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SummaryLine(label: String, value: Long, isMoney: Boolean = true) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(if (isMoney) money(value) else value.toString(), fontWeight = FontWeight.Bold)
    }
}

private enum class AnalysisChartMode { PIE, TIMELINE }

private data class CategoryExpenseSlice(
    val category: String,
    val amountInCents: Long
)

private data class TimelineExpensePoint(
    val date: LocalDate,
    val amountInCents: Long
)

private data class MovementDateGroup(
    val title: String,
    val movements: List<FinancialMovement>
)

private fun currentMonthExpenses(movements: List<FinancialMovement>): List<FinancialMovement> {
    val month = YearMonth.now()
    return movements.filter { it.type == FinancialMovementType.EXPENSE && YearMonth.from(it.date) == month }
}

private fun expenseCategorySlices(movements: List<FinancialMovement>): List<CategoryExpenseSlice> {
    return movements
        .groupBy { normalizedCategory(it) }
        .map { (category, items) -> CategoryExpenseSlice(category, items.sumOf { it.amountInCents }) }
        .filter { it.amountInCents > 0L }
        .sortedByDescending { it.amountInCents }
}

private fun expenseTimelinePoints(movements: List<FinancialMovement>): List<TimelineExpensePoint> {
    return movements
        .groupBy { it.date }
        .map { (date, items) -> TimelineExpensePoint(date, items.sumOf { it.amountInCents }) }
        .sortedBy { it.date }
}

private fun normalizedCategory(movement: FinancialMovement): String {
    return movement.category?.takeIf { it.isNotBlank() } ?: movement.type.displayName
}

private fun groupMovementsByDate(movements: List<FinancialMovement>): List<MovementDateGroup> {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val currentMonth = YearMonth.now()
    val todayItems = movements.filter { it.date == today }
    val yesterdayItems = movements.filter { it.date == yesterday }
    val thisMonthItems = movements.filter { it.date != today && it.date != yesterday && YearMonth.from(it.date) == currentMonth }
    val previousItems = movements.filter { YearMonth.from(it.date) != currentMonth }

    return listOf(
        MovementDateGroup("Hoje", todayItems),
        MovementDateGroup("Ontem", yesterdayItems),
        MovementDateGroup("Este mês", thisMonthItems),
        MovementDateGroup("Anteriores", previousItems)
    ).filter { it.movements.isNotEmpty() }
}

private fun resolveAccountName(movement: FinancialMovement, accounts: List<FinancialAccount>): String {
    val id = movement.accountId ?: movement.fromAccountId ?: movement.toAccountId
    return accounts.firstOrNull { it.id == id }?.name ?: "Conta não informada"
}

private fun categoryIcon(movement: FinancialMovement): ImageVector {
    val category = movement.category.orEmpty().lowercase(Locale.getDefault())
    return when {
        movement.type == FinancialMovementType.TRANSFER -> Icons.Default.SwapHoriz
        "aliment" in category || "food" in category -> Icons.Default.Fastfood
        "casa" in category || "moradia" in category || "aluguel" in category -> Icons.Default.Home
        "saúde" in category || "saude" in category -> Icons.Default.LocalHospital
        "compr" in category || "shopping" in category -> Icons.Default.ShoppingCart
        "salário" in category || "salario" in category || "trabalho" in category -> Icons.Default.Work
        movement.type == FinancialMovementType.INCOME -> Icons.Default.AttachMoney
        else -> Icons.Default.Category
    }
}

private fun chartColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF6750A4),
        Color(0xFF006A6A),
        Color(0xFFB3261E),
        Color(0xFF7D5260),
        Color(0xFF386A20),
        Color(0xFF8C5000),
        Color(0xFF005DBA),
        Color(0xFF6D5E00)
    )
    return colors[index % colors.size]
}

private fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)
}

private fun String.filterMoneyChars(): String = filter { it.isDigit() || it == ',' || it == '.' }

private fun LocalDate.toEpochMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
