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

    // 磁盘 HTTP 缓存：让重复打开 / 刷新瞬间返回上次结果，近似 Chrome 的“先缓存后更新”
    private val httpCache = Cache(File(appContext.cacheDir, "http_cache"), 5L * 1024 * 1024)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .cache(httpCache)
        .addInterceptor(AuthInterceptor(settingsRepository))
        // 迫使响应可缓存：15 秒内直接命中缓存，60 秒内先给旧值并后台重新校验
        .addNetworkInterceptor { chain ->
            val response = chain.proceed(chain.request())
            response.newBuilder()
                .header("Cache-Control", "public, max-age=15, stale-while-revalidate=60")
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
