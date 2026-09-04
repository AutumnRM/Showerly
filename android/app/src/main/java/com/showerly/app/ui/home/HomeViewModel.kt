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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.HttpException
import java.net.UnknownHostException
import java.net.SocketTimeoutException
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
    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            // 请求相关设置变化时刷新；深色模式切换不会触发网络请求。
            repo.settings
                .map { it.toLoadConfig() }
                .distinctUntilChanged()
                .collect { config ->
                    startRefresh(config, forceNetwork = false)
                }
        }
    }

    fun refresh() {
        startRefresh(repo.current.toLoadConfig(), forceNetwork = true)
    }

    private fun startRefresh(config: LoadConfig, forceNetwork: Boolean) {
        refreshJob?.cancel()

        val selectionChanged = _uiState.value.gender != config.gender ||
            _uiState.value.campus != config.campus
        _uiState.update {
            it.copy(
                isLoading = true,
                bathrooms = if (selectionChanged) emptyList() else it.bathrooms,
                gender = config.gender,
                campus = config.campus,
                error = null
            )
        }

        if (!config.campus.supported) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    bathrooms = emptyList(),
                    error = "${config.campus.label}暂不可用"
                )
            }
            return
        }
        if (config.endpoint.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "校方接口地址为空") }
            return
        }

        refreshJob = viewModelScope.launch {
            try {
                val response = api.getCrowd(
                    url = buildUrl(config),
                    headers = buildHeaders(config, forceNetwork)
                )
                if (response.code != null && response.code != "200") {
                    error(response.msg?.takeIf { it.isNotBlank() } ?: "服务返回异常状态")
                }
                val source = response.data ?: error(
                    response.msg?.takeIf { it.isNotBlank() } ?: "服务未返回浴室数据"
                )
                val filtered = filter(source, config.gender)
                _uiState.value = HomeUiState(
                    isLoading = false,
                    bathrooms = filtered.map { it.toStatus() },
                    gender = config.gender,
                    campus = config.campus,
                    timeText = formatNow(),
                    error = if (filtered.isEmpty()) "当前筛选下暂无浴室" else null
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                // 保留同一筛选条件下的已有数据，失败提示由界面内联展示。
                _uiState.update {
                    it.copy(isLoading = false, error = e.toUserMessage())
                }
            }
        }
    }

    private fun buildUrl(config: LoadConfig): String = config.endpoint
        .toHttpUrl()
        .newBuilder()
        .setQueryParameter("campusId", config.campus.campusId)
        .build()
        .toString()

    private fun buildHeaders(config: LoadConfig, forceNetwork: Boolean): Map<String, String> {
        val ts = System.currentTimeMillis().toString()
        val headers = mutableMapOf(
            "accept" to "application/json",
            "accept-language" to "zh-CN,zh;q=0.8",
            "user-agent" to "okhttp-okgo/jeasonlzy",
            "timestamp" to ts,
            "requestid" to "$ts-${UUID.randomUUID()}",
            "os" to "android",
            "versionno" to "120",
            // 用户主动刷新时必须重新校验，避免 15 秒 HTTP 缓存让刷新看起来无效。
            "Cache-Control" to if (forceNetwork) "no-cache" else "max-age=15"
        )
        if (config.authHeaderValue.isNotBlank()) {
            val name = config.authHeaderName.ifBlank { "Authorization" }
            val value = if (config.authHeaderValue.contains(" ")) {
                config.authHeaderValue
            } else {
                "Bearer ${config.authHeaderValue}"
            }
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

    private fun Throwable.toUserMessage(): String = when (this) {
        is UnknownHostException -> "网络不可用，请检查连接后重试"
        is SocketTimeoutException -> "连接超时，请稍后重试"
        is HttpException -> "服务暂时不可用（HTTP ${code()}）"
        is IllegalArgumentException -> "接口地址无效"
        else -> message?.takeIf { it.isNotBlank() } ?: "刷新失败，请稍后重试"
    }

    private data class LoadConfig(
        val endpoint: String,
        val authHeaderName: String,
        val authHeaderValue: String,
        val gender: Gender,
        val campus: Campus
    )

    private fun AppSettings.toLoadConfig() = LoadConfig(
        endpoint = endpoint.trim(),
        authHeaderName = authHeaderName.trim(),
        authHeaderValue = authHeaderValue.trim(),
        gender = genderEnum,
        campus = campusEnum
    )

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { HomeViewModel(container) }
        }
    }
}
