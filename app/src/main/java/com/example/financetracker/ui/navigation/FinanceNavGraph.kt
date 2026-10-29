package com.example.financetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.financetracker.ui.entry.EntryScreen
import com.example.financetracker.ui.home.HomeScreen
import com.example.financetracker.ui.settings.SettingsScreen

/*
 * ҮҮРЭГ: Навигаци — аль дэлгэц ямар "хаяг" (route)-тай, хоорондоо яаж шилжихийг тодорхойлно.
 *
 *   "home"                        → Үндсэн дэлгэц (апп асахад эхэлнэ)
 *   "entry"                       → Бүртгэл НЭМЭХ  (id өгөөгүй → NEW_ID = -1)
 *   "entry?transactionId=5"       → 5 дугаар бүртгэлийг ЗАСАХ
 *   "settings"                    → Тохиргоо
 *
 * Нэмэх, засах хоёр НЭГ дэлгэц ашиглана — ялгаа нь зөвхөн id дамжуулсан эсэх.
 */

object HomeDestination {
    const val route = "home"
}

object EntryDestination {
    const val route = "entry"
    const val ARG_ID = "transactionId"
    const val NEW_ID = -1

    const val routeWithArgs = "$route?$ARG_ID={$ARG_ID}"

    fun createRoute(id: Int? = null): String =
        if (id == null) route else "$route?$ARG_ID=$id"
}

object SettingsDestination {
    const val route = "settings"
}

@Composable
fun FinanceNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route, //app undsen delgets
        modifier = modifier
    ) {
        // 1) HomeScreen
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToAdd = {
                    navController.navigate(EntryDestination.route)
                },
                navigateToEdit = { id ->
                    navController.navigate(EntryDestination.createRoute(id))
                },
                navigateToSettings = {
                    navController.navigate(SettingsDestination.route)
                })
        }
        // 2) Add/edit screen
        composable(
            route = EntryDestination.routeWithArgs,
            arguments = listOf(
                navArgument(EntryDestination.ARG_ID) {
                    type = NavType.IntType
                    defaultValue = EntryDestination.NEW_ID
                })
        ) {
            EntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        // 3) Settings screen
        composable(route = SettingsDestination.route) {
            SettingsScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}

