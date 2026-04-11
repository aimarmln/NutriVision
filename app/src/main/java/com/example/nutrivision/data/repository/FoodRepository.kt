package com.example.nutrivision.data.repository

import com.example.nutrivision.data.remote.api.FoodService
import com.example.nutrivision.utils.toResult
import okhttp3.MultipartBody

class FoodRepository(
    private val foodService: FoodService
) {

    suspend fun getFoodsList(q: String?, page: Int, limit: Int) =
        foodService.getFoodsList(q, page, limit).toResult()

    suspend fun getFoodDetail(foodId: String) =
        foodService.getFoodDetail(foodId).toResult()

    suspend fun detectFoods(image: MultipartBody.Part) =
        foodService.detectFoods(image).toResult()
}