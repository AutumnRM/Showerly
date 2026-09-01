package com.showerly.app.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.showerly.app.data.remote.AuthInterceptor
import com.showerly.app.data.remote.SchoolApiService
import com.showerly.app.data.settings.SettingsRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "showerly_settings")

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val settingsRepository = SettingsRepository(appContext.dataStore)

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor(settingsRepository))
        .build()

    val schoolApiService: SchoolApiService = Retrofit.Builder()
        // baseUrl 仅占位，实际请求使用 @Url 传入的完整地址
        .baseUrl("https://example.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(SchoolApiService::class.java)
}
