package com.unotangozero.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.unotangozero.app.presentation.accounts.AccountsRoute
import com.unotangozero.app.presentation.budget.EnvelopeBudgetRoute
import com.unotangozero.app.presentation.categories.FinancialCategoriesRoute
import com.unotangozero.app.presentation.debts.DebtsRoute
import com.unotangozero.app.presentation.finance.FinanceRoute
import com.unotangozero.app.presentation.goals.SavingsGoalsRoute
import com.unotangozero.app.presentation.habits.HabitsRoute
import com.unotangozero.app.presentation.movements.MovementsRoute
import com.unotangozero.app.presentation.projects.ProjectsRoute
import com.unotangozero.app.presentation.projection.FutureBalanceProjectionRoute
import com.unotangozero.app.presentation.reconciliation.ReconciliationRoute
import com.unotangozero.app.presentation.reports.FinancialReportsRoute
import com.unotangozero.app.presentation.tasks.TasksRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TangoTheme { TangoAppRoot() } }
    }
}

private data class TangoDestination(val label: String, val icon: ImageVector)

private enum class TaskDestination { MAIN, PROJECTS }
private enum class FinanceDestination { DASHBOARD, ACCOUNTS, CATEGORIES, MOVEMENTS, RECONCILIATION, BUDGET, REPORTS, PROJECTION, DEBTS }

private val destinations = listOf(
    TangoDestination("Metas", Icons.Default.CheckCircle),
    TangoDestination("Tarefas", Icons.Default.Event),
    TangoDestination("Hábitos", Icons.Default.MoreHoriz),
    TangoDestination("Finanças", Icons.Default.AccountBalanceWallet)
)

private val TangoNavy = Color(0xFF071237)
private val TangoBlue = Color(0xFF12AEEA)
private val TangoBackground = Color(0xFFF6F6F7)
private val TangoCard = Color(0xFFFFFFFF)
private val TangoMuted = Color(0xFF9095A3)
private val TangoLine = Color(0xFFE2E4EA)
private val TangoBlueSoft = Color(0xFFE6F7FE)

private val TangoColorScheme = lightColorScheme(
    primary = TangoBlue,
    onPrimary = Color.White,
    primaryContainer = TangoBlueSoft,
    onPrimaryContainer = TangoNavy,
    secondary = TangoNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9ECF5),
    onSecondaryContainer = TangoNavy,
    background = TangoBackground,
    onBackground = TangoNavy,
    surface = TangoCard,
    onSurface = TangoNavy,
    surfaceVariant = TangoCard,
    onSurfaceVariant = TangoMuted,
    outline = TangoLine,
    error = Color(0xFFE45B4F),
    onError = Color.White
)

private val TangoShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

private val TangoTypography = Typography(
    headlineLarge = TextStyle(color = TangoNavy, fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.ExtraBold),
    headlineMedium = TextStyle(color = TangoNavy, fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold),
    titleLarge = TextStyle(color = TangoNavy, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold),
    titleMedium = TextStyle(color = TangoNavy, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(color = TangoNavy, fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(color = TangoNavy, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(color = TangoNavy, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold)
)

@Composable
private fun TangoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TangoColorScheme,
        typography = TangoTypography,
        shapes = TangoShapes,
        content = content
    )
}

@Composable
private fun TangoAppRoot() {
    RequestNotificationPermissionOnce()

    var selectedIndex by remember { mutableIntStateOf(0) }
    var taskDestination by remember { mutableStateOf(TaskDestination.MAIN) }
    var financeDestination by remember { mutableStateOf(FinanceDestination.DASHBOARD) }
    var openMovementFormNext by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp
            ) {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            if (index == 1) taskDestination = TaskDestination.MAIN
                            if (index == 3) financeDestination = FinanceDestination.DASHBOARD
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
            when (selectedIndex) {
                0 -> SavingsGoalsRoute()
                1 -> when (taskDestination) {
                    TaskDestination.MAIN -> TasksRoute(
                        onOpenProjects = { taskDestination = TaskDestination.PROJECTS }
                    )
                    TaskDestination.PROJECTS -> ProjectsRoute()
                }
                2 -> HabitsRoute()
                3 -> when (financeDestination) {
                    FinanceDestination.DASHBOARD -> FinanceRoute(
                        onOpenAccounts = { financeDestination = FinanceDestination.ACCOUNTS },
                        onOpenMovements = {
                            openMovementFormNext = true
                            financeDestination = FinanceDestination.MOVEMENTS
                        },
                        onOpenBudget = { financeDestination = FinanceDestination.BUDGET },
                        onOpenGoals = { selectedIndex = 0 },
                        onOpenDebts = { financeDestination = FinanceDestination.DEBTS },
                        onOpenReports = { financeDestination = FinanceDestination.REPORTS },
                        onOpenProjection = { financeDestination = FinanceDestination.PROJECTION },
                        onOpenReconciliation = { financeDestination = FinanceDestination.RECONCILIATION },
                        onOpenCategories = { financeDestination = FinanceDestination.CATEGORIES }
                    )
                    FinanceDestination.ACCOUNTS -> AccountsRoute()
                    FinanceDestination.CATEGORIES -> FinancialCategoriesRoute()
                    FinanceDestination.MOVEMENTS -> MovementsRoute(openFormInitially = openMovementFormNext)
                    FinanceDestination.RECONCILIATION -> ReconciliationRoute()
                    FinanceDestination.BUDGET -> EnvelopeBudgetRoute()
                    FinanceDestination.REPORTS -> FinancialReportsRoute()
                    FinanceDestination.PROJECTION -> FutureBalanceProjectionRoute()
                    FinanceDestination.DEBTS -> DebtsRoute()
                }
            }
        }
    }
}

@Composable
private fun RequestNotificationPermissionOnce() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    LaunchedEffect(Unit) {
        val permissionStatus = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        )
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
