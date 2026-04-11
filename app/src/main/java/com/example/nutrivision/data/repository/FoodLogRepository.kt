package com.example.nutrivision.data.repository

import com.example.nutrivision.data.remote.api.FoodLogService
import com.example.nutrivision.data.remote.request.FoodLogRequest
import com.example.nutrivision.data.remote.request.foodlog.LogFoodRequest
import com.example.nutrivision.data.remote.request.foodlog.UpdateFoodLogRequest
import com.example.nutrivision.data.remote.response.ApiResponse
import com.example.nutrivision.data.remote.response.foodlog.FoodLogDetailResponse
import com.example.nutrivision.utils.toResult

class FoodLogRepository(
    private val foodLogService: FoodLogService
) {

    suspend fun logFood(body: LogFoodRequest) =
        foodLogService.logFood(body).toResult()

    suspend fun getFoodLogDetail(foodLogId: String) =
        foodLogService.getFoodLogDetail(foodLogId).toResult()

    suspend fun updateFoodLog(foodLogId: String, body: UpdateFoodLogRequest) =
        foodLogService.updateFoodLog(foodLogId, body).toResult()

    suspend fun deleteFoodLog(foodLogId: String) =
        foodLogService.deleteFoodLog(foodLogId).toResult()
}