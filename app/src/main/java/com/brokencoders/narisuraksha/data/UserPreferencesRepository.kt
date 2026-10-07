package com.brokencoders.narisuraksha.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DECOY_MODE_ENABLED = booleanPreferencesKey("decoy_mode_enabled")
        val SECRET_DECOY_CODE = stringPreferencesKey("secret_decoy_code")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SHAKE_DETECTION_ENABLED = booleanPreferencesKey("shake_detection_enabled")
        val GUARDIAN_SCAN_ENABLED = booleanPreferencesKey("guardian_scan_enabled")
    }

    val isDecoyModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DECOY_MODE_ENABLED] ?: false
    }

    val secretDecoyCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SECRET_DECOY_CODE] ?: "1234"
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    val isShakeDetectionEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHAKE_DETECTION_ENABLED] ?: true
    }

    val isGuardianScanEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GUARDIAN_SCAN_ENABLED] ?: true
    }

    suspend fun setDecoyModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DECOY_MODE_ENABLED] = enabled
        }
    }

    suspend fun setSecretDecoyCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SECRET_DECOY_CODE] = code
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setShakeDetectionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHAKE_DETECTION_ENABLED] = enabled
        }
    }

    suspend fun setGuardianScanEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GUARDIAN_SCAN_ENABLED] = enabled
        }
    }
}
