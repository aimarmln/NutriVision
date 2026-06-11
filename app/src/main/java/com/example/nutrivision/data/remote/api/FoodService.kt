package com.example.nutrivision.data.remote.api

import com.example.nutrivision.data.remote.response.ApiResponse
import com.example.nutrivision.data.remote.response.PagePagination
import com.example.nutrivision.data.remote.response.food.FoodDetailResponse
import com.example.nutrivision.data.remote.response.food.FoodsListResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodService {
    @GET("/api/foods")
    suspend fun getFoodsList(
        @Query("q") q: String?,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ApiResponse<FoodsListResponse, PagePagination>>

    @GET("/api/foods/{foodId}")
    suspend fun getFoodDetail(
        @Path("foodId") foodId: Int
    ): Response<ApiResponse<FoodDetailResponse, Unit>>

    @Multipart
    @POST("/api/foods/detect")
    suspend fun detectFoods(
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<FoodsListResponse, Unit>>
}
