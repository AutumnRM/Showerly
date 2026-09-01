package com.showerly.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AppSettings(
    val schoolEndpoint: String = "",
    val authHeaderName: String = "",
    val authHeaderValue: String = "",
    val demoMode: Boolean = false
)

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    val settingsFlow: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            schoolEndpoint = prefs[KEY_ENDPOINT] ?: "",
            authHeaderName = prefs[KEY_HEADER_NAME] ?: "",
            authHeaderValue = prefs[KEY_HEADER_VALUE] ?: "",
            demoMode = prefs[KEY_DEMO] ?: false
        )
    }

    suspend fun save(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[KEY_ENDPOINT] = settings.schoolEndpoint
            prefs[KEY_HEADER_NAME] = settings.authHeaderName
            prefs[KEY_HEADER_VALUE] = settings.authHeaderValue
            prefs[KEY_DEMO] = settings.demoMode
        }
    }

    private companion object {
        val KEY_ENDPOINT = stringPreferencesKey("school_endpoint")
        val KEY_HEADER_NAME = stringPreferencesKey("auth_header_name")
        val KEY_HEADER_VALUE = stringPreferencesKey("auth_header_value")
        val KEY_DEMO = booleanPreferencesKey("demo_mode")
    }
}
