package com.unotangozero.app.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.unotangozero.app.presentation.budget.BudgetRoute
import com.unotangozero.app.presentation.dashboard.DashboardRoute
import com.unotangozero.app.presentation.finance.FinanceRoute
import com.unotangozero.app.presentation.goals.GoalsRoute

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
                        onClick = { navController.navigateTopLevel(item.route) },
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
            composable(AppRoute.Dashboard.route) { DashboardRoute() }
            composable(AppRoute.Finance.route) {
                FinanceRoute(
                    onOpenAccounts = {},
                    onOpenMovements = {},
                    onOpenBudget = { navController.navigateTopLevel(AppRoute.Budget.route) },
                    onOpenGoals = { navController.navigateTopLevel(AppRoute.Goals.route) },
                    onOpenDebts = {},
                    onOpenReports = {},
                    onOpenProjection = {},
                    onOpenReconciliation = {},
                    onOpenCategories = {}
                )
            }
            composable(AppRoute.Tasks.route) { PlaceholderScreen("Tarefas", AppRoute.Tasks.route, navController) }
            composable(AppRoute.Habits.route) { PlaceholderScreen("Hábitos", AppRoute.Habits.route, navController) }
            composable(AppRoute.Budget.route) { BudgetRoute() }
            composable(AppRoute.Goals.route) { GoalsRoute() }

            composable(
                route = DetailRoute.TaskDetail.pattern,
                arguments = listOf(navArgument(DetailRoute.TaskDetail.argumentName) { type = NavType.StringType })
            ) { backStackEntry ->
                DetailScreen(
                    title = "Detalhe da tarefa",
                    idLabel = "taskId",
                    id = backStackEntry.requiredStringArg(DetailRoute.TaskDetail.argumentName),
                    navController = navController
                )
            }

            composable(
                route = DetailRoute.HabitDetail.pattern,
                arguments = listOf(navArgument(DetailRoute.HabitDetail.argumentName) { type = NavType.StringType })
            ) { backStackEntry ->
                DetailScreen(
                    title = "Detalhe do hábito",
                    idLabel = "habitId",
                    id = backStackEntry.requiredStringArg(DetailRoute.HabitDetail.argumentName),
                    navController = navController
                )
            }

            composable(
                route = DetailRoute.FinanceDetail.pattern,
                arguments = listOf(navArgument(DetailRoute.FinanceDetail.argumentName) { type = NavType.StringType })
            ) { backStackEntry ->
                DetailScreen(
                    title = "Detalhe financeiro",
                    idLabel = "movementId",
                    id = backStackEntry.requiredStringArg(DetailRoute.FinanceDetail.argumentName),
                    navController = navController
                )
            }

            composable(
                route = DetailRoute.GoalDetail.pattern,
                arguments = listOf(navArgument(DetailRoute.GoalDetail.argumentName) { type = NavType.StringType })
            ) { backStackEntry ->
                DetailScreen(
                    title = "Detalhe da meta",
                    idLabel = "goalId",
                    id = backStackEntry.requiredStringArg(DetailRoute.GoalDetail.argumentName),
                    navController = navController
                )
            }
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

private sealed class DetailRoute(
    val pattern: String,
    val argumentName: String
) {
    data object TaskDetail : DetailRoute("task_detail/{taskId}", "taskId") {
        fun createRoute(taskId: String): String = "task_detail/$taskId"
    }

    data object HabitDetail : DetailRoute("habit_detail/{habitId}", "habitId") {
        fun createRoute(habitId: String): String = "habit_detail/$habitId"
    }

    data object FinanceDetail : DetailRoute("finance_detail/{movementId}", "movementId") {
        fun createRoute(movementId: String): String = "finance_detail/$movementId"
    }

    data object GoalDetail : DetailRoute("goal_detail/{goalId}", "goalId") {
        fun createRoute(goalId: String): String = "goal_detail/$goalId"
    }
}

@Composable
private fun PlaceholderScreen(title: String, route: String, navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        Text(
            text = "Rota atual: $route. Tela provisória criada para validar a navegação final por abas.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = { navController.navigateTopLevel(AppRoute.Dashboard.route) }) {
            Text("Voltar ao Dashboard")
        }
    }
}

@Composable
private fun DetailScreen(
    title: String,
    idLabel: String,
    id: String,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        Text(
            text = "$idLabel recebido: $id",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = { navController.popBackStack() }) {
            Text("Voltar")
        }
    }
}

private fun NavController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun androidx.navigation.NavBackStackEntry.requiredStringArg(name: String): String {
    return requireNotNull(arguments?.getString(name)) { "Argumento obrigatório ausente: $name" }
}
