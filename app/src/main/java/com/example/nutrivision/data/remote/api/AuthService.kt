package com.example.nutrivision.data.remote.api

import com.example.nutrivision.data.remote.request.auth.CheckEmailRequest
import com.example.nutrivision.data.remote.request.auth.LoginRequest
import com.example.nutrivision.data.remote.request.auth.RegisterRequest
import com.example.nutrivision.data.remote.response.ApiResponse
import com.example.nutrivision.data.remote.response.auth.LoginResponse
import com.example.nutrivision.data.remote.response.auth.RefreshTokenResponse
import com.example.nutrivision.data.remote.response.auth.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST("/api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<RegisterResponse, Unit>>

    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse, Unit>>

    @POST("/api/auth/check-email")
    suspend fun checkEmail(
        @Body request: CheckEmailRequest
    ): Response<ApiResponse<Unit, Unit>>


    @POST("/api/auth/refresh")
    suspend fun refresh(
        @Header("Authorization") refreshToken: String
    ): Response<ApiResponse<RefreshTokenResponse, Unit>>
}
