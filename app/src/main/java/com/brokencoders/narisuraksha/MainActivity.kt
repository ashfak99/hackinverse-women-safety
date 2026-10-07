package com.brokencoders.narisuraksha

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.brokencoders.narisuraksha.decoy.DecoyViewModel
import com.brokencoders.narisuraksha.ui.navigation.NariSurakshaNavGraph
import com.brokencoders.narisuraksha.ui.navigation.Screen
import com.brokencoders.narisuraksha.ui.theme.NariSurakshaTheme
import com.brokencoders.narisuraksha.ui.viewmodels.HistoryViewModel
import com.brokencoders.narisuraksha.ui.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {

    private val app by lazy { application as NariSurakshaApp }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.provideFactory(
            context = applicationContext,
            sosManager = app.sosManager,
            bleTransport = app.bleTransport,
            shakeDetector = app.shakeDetector,
            preferencesRepository = app.preferencesRepository,
            deviceIdProvider = app.deviceIdProvider
        )
    }

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModel.provideFactory(app.database.sosDao())
    }

    private val decoyViewModel: DecoyViewModel by viewModels {
        DecoyViewModel.provideFactory(
            preferencesRepository = app.preferencesRepository,
            sosManager = app.sosManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NariSurakshaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val isOnboardingCompleted by mainViewModel.isOnboardingCompleted.collectAsState()
                    val isDecoyEnabled by mainViewModel.isDecoyEnabled.collectAsState()

                    // Determine start destination
                    val startDestination = when {
                        !isOnboardingCompleted -> Screen.Onboarding.route
                        isDecoyEnabled -> Screen.Decoy.route
                        else -> Screen.Home.route
                    }

                    NariSurakshaNavGraph(
                        navController = navController,
                        startDestination = startDestination,
                        mainViewModel = mainViewModel,
                        historyViewModel = historyViewModel,
                        decoyViewModel = decoyViewModel
                    )

                    // Handle intent when opened from Alert Notification
                    handleNotificationIntent(intent, navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?, navController: androidx.navigation.NavHostController) {
        if (intent?.getBooleanExtra("OPEN_ALERT_SCREEN", false) == true) {
            val senderId = intent.getShortExtra("SENDER_ID", 0)
            val lat = intent.getFloatExtra("LAT", 0f)
            val lon = intent.getFloatExtra("LON", 0f)
            val rssi = intent.getIntExtra("RSSI", -70)

            navController.navigate(Screen.Alert.createRoute(senderId, lat, lon, rssi))
        }
    }
}
