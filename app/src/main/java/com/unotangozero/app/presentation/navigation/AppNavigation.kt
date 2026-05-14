package com.unotangozero.app.presentation.navigation

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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem(AppRoute.Dashboard.route, AppRoute.Dashboard.label, Icons.Default.Dashboard),
        BottomNavItem(AppRoute.Finance.route, AppRoute.Finance.label, Icons.Default.AccountBalanceWallet),
        BottomNavItem(AppRoute.Tasks.route, AppRoute.Tasks.label, Icons.Default.CheckCircle),
        BottomNavItem(AppRoute.Habits.route, AppRoute.Habits.label, Icons.Default.Spa),
        BottomNavItem(AppRoute.Budget.route, AppRoute.Budget.label, Icons.Default.BarChart),
        BottomNavItem(AppRoute.Goals.route, AppRoute.Goals.label, Icons.Default.Flag)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AppRoute.Dashboard.route) { DashboardScreen(navController) }
            composable(AppRoute.Finance.route) { PlaceholderScreen("Finanças", navController) }
            composable(AppRoute.Tasks.route) { PlaceholderScreen("Tarefas", navController) }
            composable(AppRoute.Habits.route) { PlaceholderScreen("Hábitos", navController) }
            composable(AppRoute.Budget.route) { PlaceholderScreen("Orçamento", navController) }
            composable(AppRoute.Goals.route) { PlaceholderScreen("Metas", navController) }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private enum class AppRoute(val route: String, val label: String) {
    Dashboard("dashboard", "Dashboard"),
    Finance("finance", "Finanças"),
    Tasks("tasks", "Tarefas"),
    Habits("habits", "Hábitos"),
    Budget("budget", "Orçamento"),
    Goals("goals", "Metas")
}

@Composable
private fun DashboardScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Dashboard", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                Text(
                    text = "Tela inicial provisória para validar navegação por abas e inicialização do app.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            NavigationGrid(navController)
        }
    }
}

@Composable
private fun NavigationGrid(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NavCard(modifier = Modifier.weight(1f), route = AppRoute.Finance, navController = navController)
            NavCard(modifier = Modifier.weight(1f), route = AppRoute.Tasks, navController = navController)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NavCard(modifier = Modifier.weight(1f), route = AppRoute.Habits, navController = navController)
            NavCard(modifier = Modifier.weight(1f), route = AppRoute.Budget, navController = navController)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NavCard(modifier = Modifier.weight(1f), route = AppRoute.Goals, navController = navController)
            NavCard(modifier = Modifier.weight(1f), route = AppRoute.Dashboard, navController = navController)
        }
    }
}

@Composable
private fun NavCard(
    modifier: Modifier = Modifier,
    route: AppRoute,
    navController: NavController
) {
    Card(
        modifier = modifier,
        onClick = {
            navController.navigate(route.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = route.label,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlaceholderScreen(title: String, navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        Text(
            text = "Tela provisória criada para validar a navegação final por abas. A implementação real pode ser conectada depois.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = {
                navController.navigate(AppRoute.Dashboard.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        ) {
            Text("Voltar ao Dashboard")
        }
    }
}
