package com.showerly.app.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.showerly.app.data.remote.AuthInterceptor
import com.showerly.app.data.remote.SchoolApiService
import com.showerly.app.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "showerly_settings")

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settingsRepository = SettingsRepository(appContext.dataStore, appScope)

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // 短时磁盘缓存用于快速恢复页面；用户主动刷新会在请求头中要求重新校验。
    private val httpCache = Cache(File(appContext.cacheDir, "http_cache"), 5L * 1024 * 1024)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .cache(httpCache)
        .addInterceptor(AuthInterceptor(settingsRepository))
        // 校方响应没有稳定的缓存策略，这里只保留一个很短的可复用窗口。
        .addNetworkInterceptor { chain ->
            val response = chain.proceed(chain.request())
            response.newBuilder()
                .header("Cache-Control", "public, max-age=15")
                .build()
        }
        .build()

    val schoolApiService: SchoolApiService = Retrofit.Builder()
        // baseUrl 仅占位，实际请求使用 @Url 传入的完整地址
        .baseUrl("https://example.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(SchoolApiService::class.java)
}
