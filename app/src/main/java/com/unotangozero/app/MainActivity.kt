package com.unotangozero.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.unotangozero.app.presentation.agenda.AgendaRoute
import com.unotangozero.app.presentation.dashboard.DashboardRoute
import com.unotangozero.app.presentation.debts.DebtsRoute
import com.unotangozero.app.presentation.finance.FinanceRoute
import com.unotangozero.app.presentation.habits.HabitsRoute
import com.unotangozero.app.presentation.notes.NotesRoute
import com.unotangozero.app.presentation.tasks.TasksRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TangoTheme {
                TangoAppRoot()
            }
        }
    }
}

private data class TangoDestination(
    val label: String,
    val icon: ImageVector
)

private val destinations = listOf(
    TangoDestination("Início", Icons.Default.Home),
    TangoDestination("Tarefas", Icons.Default.CheckCircle),
    TangoDestination("Agenda", Icons.Default.Event),
    TangoDestination("Finanças", Icons.Default.AccountBalanceWallet),
    TangoDestination("Dívidas", Icons.Default.CreditCard),
    TangoDestination("Hábitos", Icons.Default.Repeat),
    TangoDestination("Notas", Icons.Default.Notes)
)

@Composable
private fun TangoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(),
        typography = MaterialTheme.typography,
        content = content
    )
}

@Composable
private fun TangoAppRoot() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedIndex) {
                0 -> DashboardRoute()
                1 -> TasksRoute()
                2 -> AgendaRoute()
                3 -> FinanceRoute()
                4 -> DebtsRoute()
                5 -> HabitsRoute()
                6 -> NotesRoute()
            }
        }
    }
}
