package com.example.nutrivision.data.remote.api

import com.example.nutrivision.data.remote.request.user.UpdateUserProfileRequest
import com.example.nutrivision.data.remote.response.ApiResponse
import com.example.nutrivision.data.remote.response.user.DailySummaryResponse
import com.example.nutrivision.data.remote.response.user.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
interface UserService {
    @GET("/api/user/daily-summary")
    suspend fun getUserDailySummary(): Response<ApiResponse<DailySummaryResponse, Unit>>

    @GET("/api/user/profile")
    suspend fun getUserProfile(): Response<ApiResponse<UserProfileResponse, Unit>>

    @PATCH("/api/user/profile")
    suspend fun updateUserProfile(
        @Body request: UpdateUserProfileRequest
    ): Response<ApiResponse<UserProfileResponse, Unit>>
}
