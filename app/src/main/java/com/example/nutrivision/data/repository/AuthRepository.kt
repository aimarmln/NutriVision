package com.example.nutrivision.data.repository

import com.example.nutrivision.data.remote.api.AuthService
import com.example.nutrivision.data.remote.request.auth.CheckEmailRequest
import com.example.nutrivision.data.remote.request.auth.LoginRequest
import com.example.nutrivision.data.remote.request.auth.RegisterRequest
import com.example.nutrivision.utils.toResult

class AuthRepository(
    private val authService: AuthService
) {

    suspend fun register(body: RegisterRequest) =
        authService.register(body).toResult()

    suspend fun login(body: LoginRequest) =
        authService.login(body).toResult()

    suspend fun checkEmail(body: CheckEmailRequest) =
        authService.checkEmail(body).toResult()

    suspend fun refreshToken(refreshToken: String) =
        authService.refresh("Bearer $refreshToken").toResult()
}