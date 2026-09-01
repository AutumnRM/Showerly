package com.showerly.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.showerly.app.domain.model.Campus
import com.showerly.app.domain.model.DarkModePref
import com.showerly.app.domain.model.Gender
import com.showerly.app.domain.model.ThemePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AppSettings(
    val endpoint: String = AppSettings.DEFAULT_ENDPOINT,
    val authHeaderName: String = "",
    val authHeaderValue: String = "",
    val gender: String = Gender.MALE.name,
    val campus: String = Campus.CHANGAN.name,
    val darkMode: String = DarkModePref.SYSTEM.name,
    val theme: String = ThemePreset.TEAL.name
) {
    val genderEnum: Gender get() = runCatching { Gender.valueOf(gender) }.getOrDefault(Gender.MALE)
    val campusEnum: Campus get() = runCatching { Campus.valueOf(campus) }.getOrDefault(Campus.CHANGAN)
    val darkModeEnum: DarkModePref get() = runCatching { DarkModePref.valueOf(darkMode) }.getOrDefault(DarkModePref.SYSTEM)
    val themeEnum: ThemePreset get() = runCatching { ThemePreset.valueOf(theme) }.getOrDefault(ThemePreset.TEAL)

    companion object {
        const val DEFAULT_ENDPOINT = "https://cloudman.jinghaojian.net/bathroom"
    }
}

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    val settingsFlow: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            endpoint = prefs[KEY_ENDPOINT] ?: AppSettings.DEFAULT_ENDPOINT,
            authHeaderName = prefs[KEY_HEADER_NAME] ?: "",
            authHeaderValue = prefs[KEY_HEADER_VALUE] ?: "",
            gender = prefs[KEY_GENDER] ?: Gender.MALE.name,
            campus = prefs[KEY_CAMPUS] ?: Campus.CHANGAN.name,
            darkMode = prefs[KEY_DARK_MODE] ?: DarkModePref.SYSTEM.name,
            theme = prefs[KEY_THEME] ?: ThemePreset.TEAL.name
        )
    }

    suspend fun save(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[KEY_ENDPOINT] = settings.endpoint
            prefs[KEY_HEADER_NAME] = settings.authHeaderName
            prefs[KEY_HEADER_VALUE] = settings.authHeaderValue
            prefs[KEY_GENDER] = settings.gender
            prefs[KEY_CAMPUS] = settings.campus
            prefs[KEY_DARK_MODE] = settings.darkMode
            prefs[KEY_THEME] = settings.theme
        }
    }

    private companion object {
        val KEY_ENDPOINT = stringPreferencesKey("school_endpoint")
        val KEY_HEADER_NAME = stringPreferencesKey("auth_header_name")
        val KEY_HEADER_VALUE = stringPreferencesKey("auth_header_value")
        val KEY_GENDER = stringPreferencesKey("gender")
        val KEY_CAMPUS = stringPreferencesKey("campus")
        val KEY_DARK_MODE = stringPreferencesKey("dark_mode")
        val KEY_THEME = stringPreferencesKey("theme")
    }
}