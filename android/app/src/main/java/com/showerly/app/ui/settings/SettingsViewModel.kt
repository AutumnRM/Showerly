package com.showerly.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.showerly.app.data.settings.AppSettings
import com.showerly.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val endpoint: String = "",
    val headerName: String = "",
    val headerValue: String = "",
    val demoMode: Boolean = false,
    val isSaving: Boolean = false
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val s = container.settingsRepository.settingsFlow.first()
            _uiState.value = SettingsUiState(
                endpoint = s.schoolEndpoint,
                headerName = s.authHeaderName,
                headerValue = s.authHeaderValue,
                demoMode = s.demoMode
            )
        }
    }

    fun setEndpoint(v: String) { _uiState.value = _uiState.value.copy(endpoint = v) }
    fun setHeaderName(v: String) { _uiState.value = _uiState.value.copy(headerName = v) }
    fun setHeaderValue(v: String) { _uiState.value = _uiState.value.copy(headerValue = v) }
    fun setDemoMode(v: Boolean) { _uiState.value = _uiState.value.copy(demoMode = v) }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val s = _uiState.value
            _uiState.value = s.copy(isSaving = true)
            container.settingsRepository.save(
                AppSettings(
                    schoolEndpoint = s.endpoint.trim(),
                    authHeaderName = s.headerName.trim(),
                    authHeaderValue = s.headerValue.trim(),
                    demoMode = s.demoMode
                )
            )
            _uiState.value = _uiState.value.copy(isSaving = false)
            onSaved()
        }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { SettingsViewModel(container) }
        }
    }
}
