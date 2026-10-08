package com.brokencoders.narisuraksha.ui.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Decoy : Screen("decoy")
    data object Home : Screen("home")
    data object Alert : Screen("alert?senderId={senderId}&lat={lat}&lon={lon}&rssi={rssi}") {
        fun createRoute(senderId: Short, lat: Float, lon: Float, rssi: Int): String {
            return "alert?senderId=$senderId&lat=$lat&lon=$lon&rssi=$rssi"
        }
    }
    data object History : Screen("history")
    data object SafeZones : Screen("safe_zones")
    data object Settings : Screen("settings")
    data object Map : Screen("map")
    data object EmergencyContacts : Screen("emergency_contacts")
    data object BleDebug : Screen("ble_debug")
}
