package com.example.nutrivision.data.remote.api

import com.example.nutrivision.data.remote.request.recipe.PostRecipeCommentRequest
import com.example.nutrivision.data.remote.response.ApiResponse
import com.example.nutrivision.data.remote.response.CursorPagination
import com.example.nutrivision.data.remote.response.PagePagination
import com.example.nutrivision.data.remote.response.recipe.PostCommentResponse
import com.example.nutrivision.data.remote.response.recipe.RecipeCommentsResponse
import com.example.nutrivision.data.remote.response.recipe.RecipeDetailResponse
import com.example.nutrivision.data.remote.response.recipe.RecipesListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeService {
    @GET("/api/recipes")
    suspend fun getRecipesList(
        @Query("q") q: String?,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ApiResponse<RecipesListResponse, PagePagination>>

    @GET("/api/recipes/{recipeId}")
    suspend fun getRecipeDetail(
        @Path("recipeId") recipeId: String
    ): Response<ApiResponse<RecipeDetailResponse, Unit>>

    @GET("/api/recipes/{recipeId}/comments")
    suspend fun getRecipeComments(
        @Path("recipeId") recipeId: String,
        @Query("last_created_at") lastCreatedAt: String?,
        @Query("limit") limit: Int
    ): Response<ApiResponse<RecipeCommentsResponse, CursorPagination>>

    @POST("/api/recipes/{recipeId}/comments")
    suspend fun postRecipeComment(
        @Path("recipeId") recipeId: String,
        @Body request: PostRecipeCommentRequest
    ): Response<ApiResponse<PostCommentResponse, Unit>>
}
