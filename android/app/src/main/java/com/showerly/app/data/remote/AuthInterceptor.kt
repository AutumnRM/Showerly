package com.showerly.app.data.remote

import com.showerly.app.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val settings: SettingsRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val (name, value) = runBlocking {
            val s = settings.settingsFlow.first()
            s.authHeaderName.trim() to s.authHeaderValue.trim()
        }
        val newRequest = if (name.isNotEmpty() && value.isNotEmpty()) {
            request.newBuilder().header(name, value).build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }
}
