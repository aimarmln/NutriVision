package com.example.nutrivision.data.remote.network

import com.example.nutrivision.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val accessToken = runBlocking {
            tokenManager.getAccessToken()
        }

        val request = if (!accessToken.isNullOrEmpty()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}
