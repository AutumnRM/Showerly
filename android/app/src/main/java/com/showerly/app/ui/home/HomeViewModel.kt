package com.showerly.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.showerly.app.data.remote.dto.BathroomDto
import com.showerly.app.data.settings.AppSettings
import com.showerly.app.di.AppContainer
import com.showerly.app.domain.model.BathroomStatus
import com.showerly.app.domain.model.Campus
import com.showerly.app.domain.model.Gender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class HomeUiState(
    val isLoading: Boolean = true,
    val bathrooms: List<BathroomStatus> = emptyList(),
    val gender: Gender = Gender.MALE,
    val campus: Campus = Campus.CHANGAN,
    val timeText: String = "",
    val error: String? = null
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {
    private val repo = container.settingsRepository
    private val api = container.schoolApiService

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // 仅当性别/校区变化才刷新，深色模式切换不触发网络请求
            repo.settings
                .map { it.genderEnum to it.campusEnum }
                .distinctUntilChanged()
                .collect { (g, c) ->
                    _uiState.value = _uiState.value.copy(gender = g, campus = c)
                    refresh()
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val s = repo.settings.value
            val campus = s.campusEnum
            if (!campus.supported) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bathrooms = emptyList(),
                    campus = campus,
                    error = "${campus.label}接口尚未逆向，暂用长安校区数据"
                )
                return@launch
            }
            if (s.endpoint.isBlank()) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "请在设置中填写校方接口地址")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            val url = buildUrl(s)
            runCatching { api.getCrowd(url, buildHeaders(s)) }
                .onSuccess { resp ->
                    val filtered = filter(resp.data.orEmpty(), s.genderEnum)
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        bathrooms = filtered.map { it.toStatus() },
                        gender = s.genderEnum,
                        campus = campus,
                        timeText = formatNow(),
                        error = if (filtered.isEmpty()) "当前筛选下暂无浴室" else null
                    )
                }
                .onFailure { e ->
                    // 保留已有数据，仅提示错误，避免刷新时白屏
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "请求失败，请检查网络或接口"
                    )
                }
        }
    }

    private fun buildUrl(s: AppSettings): String {
        val sep = if (s.endpoint.contains("?")) "&" else "?"
        return if (s.endpoint.contains("campusId=")) s.endpoint
        else s.endpoint + sep + "campusId=" + s.campusEnum.campusId
    }

    private fun buildHeaders(s: AppSettings): Map<String, String> {
        val ts = System.currentTimeMillis().toString()
        val headers = mutableMapOf(
            "accept" to "application/json",
            "accept-language" to "zh-CN,zh;q=0.8",
            "user-agent" to "okhttp-okgo/jeasonlzy",
            "timestamp" to ts,
            "requestid" to "$ts-${UUID.randomUUID()}",
            "os" to "android",
            "versionno" to "120"
        )
        if (s.authHeaderValue.isNotBlank()) {
            val name = s.authHeaderName.ifBlank { "Authorization" }
            val value = if (s.authHeaderValue.contains(" ")) s.authHeaderValue else "Bearer ${s.authHeaderValue}"
            headers[name] = value
        }
        return headers
    }

    private fun filter(dtos: List<BathroomDto>, gender: Gender): List<BathroomDto> =
        dtos.filter { it.sex == gender.sex }

    private fun BathroomDto.toStatus(): BathroomStatus {
        val load = (maxLoad ?: 0).coerceAtLeast(0)
        val use = (useCount ?: 0).coerceAtLeast(0)
        val cap = maxOf(load, use)
        val vacant = (cap - use).coerceAtLeast(0)
        val ratio = if (cap > 0) use.toFloat() / cap else 0f
        return BathroomStatus(
            id = id ?: 0,
            name = name ?: "未知浴室",
            sex = sex ?: 2,
            maxLoad = load,
            useCount = use,
            vacant = vacant,
            capacity = cap,
            occupancyRatio = ratio.coerceIn(0f, 1f),
            statusLabel = when {
                cap == 0 -> "未知"
                ratio >= 0.9f -> "爆满"
                ratio >= 0.6f -> "较拥挤"
                ratio > 0f -> "正常"
                else -> "空闲"
            }
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
