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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.unotangozero.app.presentation.accounts.AccountsRoute
import com.unotangozero.app.presentation.agenda.AgendaRoute
import com.unotangozero.app.presentation.backup.BackupRoute
import com.unotangozero.app.presentation.bills.BillsRoute
import com.unotangozero.app.presentation.dashboard.DashboardRoute
import com.unotangozero.app.presentation.debts.DebtsRoute
import com.unotangozero.app.presentation.finance.FinanceRoute
import com.unotangozero.app.presentation.focus.FocusRoute
import com.unotangozero.app.presentation.focusmode.FocusModeRoute
import com.unotangozero.app.presentation.goals.SavingsGoalsRoute
import com.unotangozero.app.presentation.habits.HabitsRoute
import com.unotangozero.app.presentation.kanban.KanbanRoute
import com.unotangozero.app.presentation.more.MoreRoute
import com.unotangozero.app.presentation.movements.MovementsRoute
import com.unotangozero.app.presentation.notes.NotesRoute
import com.unotangozero.app.presentation.projects.ProjectsRoute
import com.unotangozero.app.presentation.settings.SettingsRoute
import com.unotangozero.app.presentation.shopping.ShoppingRoute
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

private enum class ExtraDestination { MORE, PROJECTS, FOCUS_MODE, KANBAN, FOCUS, ACCOUNTS, MOVEMENTS, BILLS, GOALS, DEBTS, HABITS, SHOPPING, NOTES, SETTINGS, BACKUP }

private val destinations = listOf(
    TangoDestination("Início", Icons.Default.Home),
    TangoDestination("Tarefas", Icons.Default.CheckCircle),
    TangoDestination("Agenda", Icons.Default.Event),
    TangoDestination("Finanças", Icons.Default.AccountBalanceWallet),
    TangoDestination("Mais", Icons.Default.MoreHoriz)
)

@Composable
private fun TangoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = androidx.compose.material3.lightColorScheme(), typography = MaterialTheme.typography, content = content)
}

@Composable
private fun TangoAppRoot() {
    RequestNotificationPermissionOnce()

    var selectedIndex by remember { mutableIntStateOf(0) }
    var extraDestination by remember { mutableStateOf(ExtraDestination.MORE) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            if (index != 4) extraDestination = ExtraDestination.MORE
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
            when (selectedIndex) {
                0 -> DashboardRoute()
                1 -> TasksRoute()
                2 -> AgendaRoute()
                3 -> FinanceRoute()
                4 -> when (extraDestination) {
                    ExtraDestination.MORE -> MoreRoute(
                        onOpenDebts = { extraDestination = ExtraDestination.DEBTS },
                        onOpenHabits = { extraDestination = ExtraDestination.HABITS },
                        onOpenShopping = { extraDestination = ExtraDestination.SHOPPING },
                        onOpenNotes = { extraDestination = ExtraDestination.NOTES },
                        onOpenSettings = { extraDestination = ExtraDestination.SETTINGS },
                        onOpenBackup = { extraDestination = ExtraDestination.BACKUP },
                        onOpenAccounts = { extraDestination = ExtraDestination.ACCOUNTS },
                        onOpenProjects = { extraDestination = ExtraDestination.PROJECTS },
                        onOpenFocus = { extraDestination = ExtraDestination.FOCUS },
                        onOpenKanban = { extraDestination = ExtraDestination.KANBAN },
                        onOpenFocusMode = { extraDestination = ExtraDestination.FOCUS_MODE },
                        onOpenMovements = { extraDestination = ExtraDestination.MOVEMENTS },
                        onOpenBills = { extraDestination = ExtraDestination.BILLS },
                        onOpenGoals = { extraDestination = ExtraDestination.GOALS }
                    )
                    ExtraDestination.PROJECTS -> ProjectsRoute()
                    ExtraDestination.FOCUS_MODE -> FocusModeRoute()
                    ExtraDestination.KANBAN -> KanbanRoute()
                    ExtraDestination.FOCUS -> FocusRoute()
                    ExtraDestination.ACCOUNTS -> AccountsRoute()
                    ExtraDestination.MOVEMENTS -> MovementsRoute()
                    ExtraDestination.BILLS -> BillsRoute()
                    ExtraDestination.GOALS -> SavingsGoalsRoute()
                    ExtraDestination.DEBTS -> DebtsRoute()
                    ExtraDestination.HABITS -> HabitsRoute()
                    ExtraDestination.SHOPPING -> ShoppingRoute()
                    ExtraDestination.NOTES -> NotesRoute()
                    ExtraDestination.SETTINGS -> SettingsRoute()
                    ExtraDestination.BACKUP -> BackupRoute()
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
