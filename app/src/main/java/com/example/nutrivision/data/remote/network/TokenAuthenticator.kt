package com.example.nutrivision.data.remote.network

import com.example.nutrivision.data.local.TokenManager
import com.example.nutrivision.data.remote.api.AuthService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val authService: AuthService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        if (responseCount(response) >= 2) {
            return null
        }

        val refreshToken = runBlocking {
            tokenManager.getRefreshToken()
        } ?: return null

        val refreshResponse = runBlocking {
            authService.refresh("Bearer $refreshToken")
        }

        val body = refreshResponse.body() ?: return null

        runBlocking {
            tokenManager.saveTokens(
                body.data?.accessToken.orEmpty(),
                body.data?.refreshToken.orEmpty()
            )
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${body.data?.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}