package com.showerly.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.showerly.app.domain.model.Campus
import com.showerly.app.domain.model.DarkModePref
import com.showerly.app.domain.model.Gender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AppSettings(
    val endpoint: String = AppSettings.DEFAULT_ENDPOINT,
    val authHeaderName: String = "",
    val authHeaderValue: String = "",
    val gender: String = Gender.MALE.name,
    val campus: String = Campus.CHANGAN.name,
    val darkMode: String = DarkModePref.SYSTEM.name
) {
    val genderEnum: Gender get() = runCatching { Gender.valueOf(gender) }.getOrDefault(Gender.MALE)
    val campusEnum: Campus
        get() {
            val c = runCatching { Campus.valueOf(campus) }.getOrDefault(Campus.CHANGAN)
            return if (c.supported) c else Campus.CHANGAN
        }
    val darkModeEnum: DarkModePref get() = runCatching { DarkModePref.valueOf(darkMode) }.getOrDefault(DarkModePref.SYSTEM)

    companion object {
        const val DEFAULT_ENDPOINT = "https://cloudman.jinghaojian.net/bathroom"
    }
}

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
    scope: CoroutineScope
) {
    private fun map(prefs: Preferences): AppSettings = AppSettings(
        endpoint = prefs[KEY_ENDPOINT] ?: AppSettings.DEFAULT_ENDPOINT,
        authHeaderName = prefs[KEY_HEADER_NAME] ?: "",
        authHeaderValue = prefs[KEY_HEADER_VALUE] ?: "",
        gender = prefs[KEY_GENDER] ?: Gender.MALE.name,
        campus = prefs[KEY_CAMPUS] ?: Campus.CHANGAN.name,
        darkMode = prefs[KEY_DARK_MODE] ?: DarkModePref.SYSTEM.name
    )

    private val raw: Flow<AppSettings> = dataStore.data.map { map(it) }

    // 冷流：首次读取或需要“保证最新”时使用（如设置页初始化）
    val settingsFlow: Flow<AppSettings> = raw

    // 内存缓存：UI collectAsState / 拦截器 current 直接用，不阻塞线程
    val settings: StateFlow<AppSettings> = raw.stateIn(scope, SharingStarted.Eagerly, AppSettings())

    val current: AppSettings get() = settings.value

    suspend fun save(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[KEY_ENDPOINT] = settings.endpoint
            prefs[KEY_HEADER_NAME] = settings.authHeaderName
            prefs[KEY_HEADER_VALUE] = settings.authHeaderValue
            prefs[KEY_GENDER] = settings.gender
            prefs[KEY_CAMPUS] = settings.campus
            prefs[KEY_DARK_MODE] = settings.darkMode
        }
    }

    private companion object {
        val KEY_ENDPOINT = stringPreferencesKey("school_endpoint")
        val KEY_HEADER_NAME = stringPreferencesKey("auth_header_name")
        val KEY_HEADER_VALUE = stringPreferencesKey("auth_header_value")
        val KEY_GENDER = stringPreferencesKey("gender")
        val KEY_CAMPUS = stringPreferencesKey("campus")
        val KEY_DARK_MODE = stringPreferencesKey("dark_mode")
    }
}
