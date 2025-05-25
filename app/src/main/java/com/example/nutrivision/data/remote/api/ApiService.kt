package com.example.nutrivision.data.remote.api

import com.example.nutrivision.data.remote.request.CheckEmailRequest
import com.example.nutrivision.data.remote.request.CommentRequest
import com.example.nutrivision.data.remote.request.FoodLogRequest
import com.example.nutrivision.data.remote.request.LoginRequest
import com.example.nutrivision.data.remote.request.SignupRequest
import com.example.nutrivision.data.remote.request.UpdateMealRequest
import com.example.nutrivision.data.remote.request.UpdateProfileRequest
import com.example.nutrivision.data.remote.response.AuthResponse
import com.example.nutrivision.data.remote.response.CheckEmailResponse
import com.example.nutrivision.data.remote.response.FoodDetailResponse
import com.example.nutrivision.data.remote.response.FoodsResponseItem
import com.example.nutrivision.data.remote.response.HomeResponse
import com.example.nutrivision.data.remote.response.MealDetailResponse
import com.example.nutrivision.data.remote.response.MessageResponse
import com.example.nutrivision.data.remote.response.NutritionCalcResponse
import com.example.nutrivision.data.remote.response.ProfileResponse
import com.example.nutrivision.data.remote.response.RecipeDetailResponse
import com.example.nutrivision.data.remote.response.RecipesResponseItem
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Auth section
    @POST("auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ) : AuthResponse

    @POST("auth/check-email")
    suspend fun checkEmail(
        @Body request: CheckEmailRequest
    ) : CheckEmailResponse

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ) : Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refresh(
        @Header("Authorization") refreshToken: String,
    ) :  AuthResponse

    // Home section
    @GET("user/")
    suspend fun home(
        @Header("Authorization") accessToken: String,
    ) : Response<HomeResponse>

    // Food section
    @GET("food/")
    suspend fun foodAll() : List<FoodsResponseItem>

    @GET("food/{food_id}")
    suspend fun foodDetail(
        @Path("food_id") foodId: Int
    ) : FoodDetailResponse

    @GET("food/nutrition-calc")
    suspend fun foodNutritionCalc(
        @Query("food_id") foodId: Int,
        @Query("weight_grams") weightGrams: Int
    ) : NutritionCalcResponse

    @Multipart
    @POST("food/predict")
    suspend fun predictFood(
        @Header("Authorization") accessToken: String,
        @Part image: MultipartBody.Part
    ) : Response<List<FoodsResponseItem>>

    @POST("food/log")
    suspend fun logFood(
        @Header("Authorization") accessToken: String,
        @Body request: FoodLogRequest
    ) : Response<MessageResponse>

    @GET("food/search")
    suspend fun searchFood(
        @Query("query") query: String,
    ) : List<FoodsResponseItem>

    // Meal section
    @GET("meal-log/{meal_id}")
    suspend fun mealDetail(
        @Header("Authorization") accessToken: String,
        @Path("meal_id") mealId: Int
    ) : Response<MealDetailResponse>

    @PUT("meal-log/{meal_id}")
    suspend fun updateMeal(
        @Header("Authorization") accessToken: String,
        @Path("meal_id") mealId: Int,
        @Body request: UpdateMealRequest
    ) : Response<MessageResponse>

    @DELETE("meal-log/{meal_id}")
    suspend fun deleteMeal(
        @Header("Authorization") accessToken: String,
        @Path("meal_id") mealId: Int,
    ) : Response<MessageResponse>

    // Recipe section
    @GET("recipe/")
    suspend fun recipeAll() : List<RecipesResponseItem>

    @GET("recipe/{recipe_id}")
    suspend fun recipeDetail(
        @Path("recipe_id") recipeId: Int
    ) : RecipeDetailResponse

    @GET("recipe/search")
    suspend fun searchRecipe(
        @Query("query") query: String,
    ) : List<RecipesResponseItem>

    // Comment Section
    @POST("comment/")
    suspend fun postComment(
        @Header("Authorization") accessToken: String,
        @Body request: CommentRequest
    ) : Response<MessageResponse>

    // Profile section
    @GET("user/profile")
    suspend fun userProfile(
        @Header("Authorization") accessToken: String,
    ) : Response<ProfileResponse>

    @PUT("user/profile")
    suspend fun updateUserProfile(
        @Header("Authorization") accessToken: String,
        @Body request: UpdateProfileRequest
    ) : Response<MessageResponse>
}