package com.showerly.app.data.remote

import com.showerly.app.data.settings.SettingsRepository
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val settings: SettingsRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // 使用内存缓存的最新设置，避免在 OkHttp 线程上 runBlocking 读 DataStore
        val s = settings.current
        val name = s.authHeaderName.trim()
        val value = s.authHeaderValue.trim()
        val newRequest = if (name.isNotEmpty() && value.isNotEmpty()) {
            val header = if (value.contains(" ")) value else "Bearer $value"
            request.newBuilder().header(name, header).build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }
}
