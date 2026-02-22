package com.example.fangbianjizhang.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fangbianjizhang.ui.asset.AssetScreen
import com.example.fangbianjizhang.ui.home.HomeScreen
import com.example.fangbianjizhang.ui.record.RecordScreen
import com.example.fangbianjizhang.ui.settings.AccountManageScreen
import com.example.fangbianjizhang.ui.settings.BudgetSettingScreen
import com.example.fangbianjizhang.ui.settings.CategoryManageScreen
import com.example.fangbianjizhang.ui.settings.RecurringManageScreen
import com.example.fangbianjizhang.ui.settings.SettingsScreen
import com.example.fangbianjizhang.ui.statistics.StatisticsScreen

private const val DURATION = 260

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.RECORD) },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "记账")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(DURATION)) + slideInHorizontally(tween(DURATION)) { it / 5 } },
            exitTransition = { fadeOut(tween(DURATION / 2)) },
            popEnterTransition = { fadeIn(tween(DURATION)) + slideInHorizontally(tween(DURATION)) { -it / 5 } },
            popExitTransition = { fadeOut(tween(DURATION / 2)) + slideOutHorizontally(tween(DURATION)) { it / 5 } }
        ) {
            composable(Routes.HOME) {
                HomeScreen(onEditTransaction = { txId ->
                    navController.navigate(Routes.recordEdit(txId))
                })
            }
            composable(Routes.ASSET) { AssetScreen() }
            composable(Routes.STATISTICS) { StatisticsScreen() }
            composable(Routes.SETTINGS) { SettingsScreen(navController) }
            composable(Routes.RECORD) { RecordScreen(onBack = { navController.popBackStack() }) }
            composable(
                Routes.RECORD_EDIT,
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { RecordScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.CATEGORY_MANAGE) { CategoryManageScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.BUDGET_SETTING) { BudgetSettingScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.RECURRING_MANAGE) { RecurringManageScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.ACCOUNT_MANAGE) { AccountManageScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
