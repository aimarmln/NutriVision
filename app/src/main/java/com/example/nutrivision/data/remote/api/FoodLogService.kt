package com.example.nutrivision.data.remote.api

import com.example.nutrivision.data.remote.request.foodlog.LogFoodRequest
import com.example.nutrivision.data.remote.request.foodlog.UpdateFoodLogRequest
import com.example.nutrivision.data.remote.response.ApiResponse
import com.example.nutrivision.data.remote.response.foodlog.FoodLogDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FoodLogService {
    @POST("/api/food-logs")
    suspend fun logFood(
        @Body request: LogFoodRequest
    ): Response<ApiResponse<Unit, Unit>>

    @GET("/api/food-logs/{foodLogId}")
    suspend fun getFoodLogDetail(
        @Path("foodLogId") foodLogId: Int
    ): Response<ApiResponse<FoodLogDetailResponse, Unit>>

    @PUT("/api/food-logs/{foodLogId}")
    suspend fun updateFoodLog(
        @Path("foodLogId") foodLogId: Int,
        @Body request: UpdateFoodLogRequest
    ): Response<ApiResponse<Unit, Unit>>

    @DELETE("/api/food-logs/{foodLogId}")
    suspend fun deleteFoodLog(
        @Path("foodLogId") foodLogId: Int
    ): Response<ApiResponse<Unit, Unit>>
}