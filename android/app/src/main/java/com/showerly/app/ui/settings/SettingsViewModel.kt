package com.showerly.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.showerly.app.di.AppContainer
import com.showerly.app.domain.model.Campus
import com.showerly.app.domain.model.DarkModePref
import com.showerly.app.domain.model.Gender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val gender: Gender = Gender.MALE,
    val campus: Campus = Campus.CHANGAN,
    val darkMode: DarkModePref = DarkModePref.SYSTEM
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val s = container.settingsRepository.settingsFlow.first()
            _uiState.value = SettingsUiState(
                gender = s.genderEnum,
                campus = s.campusEnum,
                darkMode = s.darkModeEnum
            )
        }
    }

    fun setGender(v: Gender) { _uiState.value = _uiState.value.copy(gender = v); persist() }
    fun setCampus(v: Campus) {
        if (v.supported) {
            _uiState.value = _uiState.value.copy(campus = v)
            persist()
        }
    }
    fun setDarkMode(v: DarkModePref) { _uiState.value = _uiState.value.copy(darkMode = v); persist() }

    // 仅合并 UI 控制的字段，接口/auth 等保持现状。
    private fun persist() {
        val s = _uiState.value
        viewModelScope.launch {
            val current = container.settingsRepository.settingsFlow.first()
            container.settingsRepository.save(
                current.copy(
                    gender = s.gender.name,
                    campus = s.campus.name,
                    darkMode = s.darkMode.name
                )
            )
        }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { SettingsViewModel(container) }
        }
    }
}