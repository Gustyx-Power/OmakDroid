package id.xms.omakdroid.core

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SettingsRepository(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        private val ORIENTATION_KEY = intPreferencesKey("screen_orientation")
        private val IS_SETUP_COMPLETE_KEY = booleanPreferencesKey("is_setup_complete")
        const val DEFAULT_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    val orientationFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[ORIENTATION_KEY] ?: DEFAULT_ORIENTATION
        }

    val isSetupCompleteFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_SETUP_COMPLETE_KEY] ?: false
        }
    
    suspend fun saveOrientation(orientation: Int) {
        context.dataStore.edit { preferences ->
            preferences[ORIENTATION_KEY] = orientation
        }
    }
    
    suspend fun markSetupComplete() {
        context.dataStore.edit { preferences ->
            preferences[IS_SETUP_COMPLETE_KEY] = true
        }
    }
    
    suspend fun resetSetup() {
        context.dataStore.edit { preferences ->
            preferences[IS_SETUP_COMPLETE_KEY] = false
        }
    }
}
