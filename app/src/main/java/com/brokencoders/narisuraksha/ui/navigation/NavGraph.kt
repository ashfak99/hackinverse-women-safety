package com.brokencoders.narisuraksha.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.brokencoders.narisuraksha.decoy.CalculatorScreen
import com.brokencoders.narisuraksha.decoy.DecoyViewModel
import com.brokencoders.narisuraksha.ui.screens.AlertScreen
import com.brokencoders.narisuraksha.ui.screens.HistoryScreen
import com.brokencoders.narisuraksha.ui.screens.HomeScreen
import com.brokencoders.narisuraksha.ui.screens.OnboardingScreen
import com.brokencoders.narisuraksha.ui.screens.SafeZoneScreen
import com.brokencoders.narisuraksha.ui.viewmodels.HistoryViewModel
import com.brokencoders.narisuraksha.ui.viewmodels.MainViewModel

@Composable
fun NariSurakshaNavGraph(
    navController: NavHostController,
    startDestination: String,
    mainViewModel: MainViewModel,
    historyViewModel: HistoryViewModel,
    decoyViewModel: DecoyViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = { customPin ->
                    mainViewModel.updateSecretDecoyCode(customPin)
                    mainViewModel.setOnboardingCompleted(true)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Decoy.route) {
            CalculatorScreen(
                viewModel = decoyViewModel,
                onUnlockApp = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Decoy.route) { inclusive = true }
                    }
                },
                onSosTriggered = {
                    // Silently triggers SOS, optionally show home screen
                    navController.navigate(Screen.Home.route)
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = mainViewModel,
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSafeZones = { navController.navigate(Screen.SafeZones.route) },
                onNavigateToDecoy = { navController.navigate(Screen.Decoy.route) },
                onNavigateToAlert = { senderId, lat, lon, rssi ->
                    navController.navigate(Screen.Alert.createRoute(senderId, lat, lon, rssi))
                }
            )
        }

        composable(
            route = Screen.Alert.route,
            arguments = listOf(
                navArgument("senderId") { type = NavType.IntType; defaultValue = 0 },
                navArgument("lat") { type = NavType.FloatType; defaultValue = 0f },
                navArgument("lon") { type = NavType.FloatType; defaultValue = 0f },
                navArgument("rssi") { type = NavType.IntType; defaultValue = -70 }
            )
        ) { backStackEntry ->
            val senderId = (backStackEntry.arguments?.getInt("senderId") ?: 0).toShort()
            val lat = backStackEntry.arguments?.getFloat("lat") ?: 0f
            val lon = backStackEntry.arguments?.getFloat("lon") ?: 0f
            val rssi = backStackEntry.arguments?.getInt("rssi") ?: -70

            AlertScreen(
                senderId = senderId,
                lat = lat,
                lon = lon,
                rssi = rssi,
                viewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SafeZones.route) {
            SafeZoneScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
