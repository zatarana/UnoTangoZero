package com.unotangozero.app.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold { paddingValues ->
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
                    text = "Tela inicial provisória para validar navegação e inicialização do app.",
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
            text = "Tela provisória criada para validar a navegação básica. A implementação real pode ser conectada depois.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = { navController.navigate(AppRoute.Dashboard.route) { launchSingleTop = true } }) {
            Text("Voltar ao Dashboard")
        }
    }
}
