package com.flowpebble.microguides.data.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flowpebble_prefs")

data class UserPrefs(
    val themeMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
    val hapticsEnabled: Boolean = true,
    val bonusVaultUnlocked: Boolean = false,
    val onboardingCompleted: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val BONUS_UNLOCKED = booleanPreferencesKey("bonus_vault_unlocked")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userPrefsFlow: Flow<UserPrefs> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            UserPrefs(
                themeMode = prefs[Keys.THEME_MODE] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
                bonusVaultUnlocked = prefs[Keys.BONUS_UNLOCKED] ?: false,
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
            )
        }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun unlockBonusVault() {
        context.dataStore.edit { prefs ->
            prefs[Keys.BONUS_UNLOCKED] = true
        }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }
}
