package com.showerly.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.showerly.app.data.remote.dto.CrowdApiResponse
import com.showerly.app.data.settings.SettingsRepository
import com.showerly.app.di.AppContainer
import com.showerly.app.domain.model.CrowdStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val isLoading: Boolean = false,
    val crowd: CrowdStatus? = null,
    val error: String? = null
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {
    private val settings: SettingsRepository = container.settingsRepository
    private val api = container.schoolApiService

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val s = settings.settingsFlow.first()
            if (s.demoMode) {
                _uiState.value = HomeUiState(isLoading = false, crowd = demoCrowd())
                return@launch
            }
            if (s.schoolEndpoint.isBlank()) {
                _uiState.value = HomeUiState(isLoading = false, error = "请在设置中填写校方接口地址")
                return@launch
            }
            _uiState.value = HomeUiState(isLoading = true)
            runCatching { api.getCrowd(s.schoolEndpoint) }
                .onSuccess { resp ->
                    _uiState.value = HomeUiState(isLoading = false, crowd = mapToCrowd(resp))
                }
                .onFailure { e ->
                    _uiState.value = HomeUiState(isLoading = false, error = e.message ?: "请求失败")
                }
        }
    }

    private fun mapToCrowd(resp: CrowdApiResponse): CrowdStatus {
        val d = resp.data
        val total = d?.total ?: d?.count ?: d?.current ?: d?.used ?: resp.total ?: 0
        val capacity = d?.capacity ?: d?.totalBays ?: resp.capacity ?: 0
        val ratio = if (capacity > 0) total.toFloat() / capacity else 0f
        val timeText = d?.time ?: resp.time ?: formatNow()
        return CrowdStatus(
            total = total,
            capacity = capacity,
            occupancyRatio = ratio.coerceIn(0f, 1f),
            timeText = timeText,
            statusLabel = when {
                capacity > 0 && ratio >= 0.9f -> "爆满"
                capacity > 0 && ratio >= 0.6f -> "较拥挤"
                capacity > 0 && ratio > 0f -> "正常"
                else -> "未知"
            }
        )
    }

    private fun demoCrowd(): CrowdStatus {
        val total = 62
        val capacity = 80
        return CrowdStatus(
            total = total,
            capacity = capacity,
            occupancyRatio = total.toFloat() / capacity,
            timeText = formatNow(),
            statusLabel = "较拥挤",
            isDemo = true
        )
    }

    private fun formatNow(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { HomeViewModel(container) }
        }
    }
}
