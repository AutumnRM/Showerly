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
import kotlinx.coroutines.flow.collectLatest
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
            container.settingsRepository.settings.collectLatest { s ->
                _uiState.value = SettingsUiState(
                    gender = s.genderEnum,
                    campus = s.campusEnum,
                    darkMode = s.darkModeEnum
                )
            }
        }
    }

    fun setGender(v: Gender) {
        _uiState.value = _uiState.value.copy(gender = v)
        viewModelScope.launch { container.settingsRepository.updatePreferences(gender = v) }
    }

    fun setCampus(v: Campus) {
        if (v.supported) {
            _uiState.value = _uiState.value.copy(campus = v)
            viewModelScope.launch { container.settingsRepository.updatePreferences(campus = v) }
        }
    }

    fun setDarkMode(v: DarkModePref) {
        _uiState.value = _uiState.value.copy(darkMode = v)
        viewModelScope.launch { container.settingsRepository.updatePreferences(darkMode = v) }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { SettingsViewModel(container) }
        }
    }
}
