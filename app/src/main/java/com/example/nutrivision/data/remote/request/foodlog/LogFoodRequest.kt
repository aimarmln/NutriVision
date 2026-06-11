package com.example.nutrivision.data.remote.request.foodlog


import com.google.gson.annotations.SerializedName

data class LogFoodRequest(
    @SerializedName("food_id")
    val foodId: Int,
    @SerializedName("meal_type")
    val mealType: String,
    @SerializedName("number_of_units")
    val numberOfUnits: Float,
    @SerializedName("serving_id")
    val servingId: Int
)