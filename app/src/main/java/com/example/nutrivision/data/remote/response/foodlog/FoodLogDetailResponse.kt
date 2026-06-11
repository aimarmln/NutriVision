package com.example.nutrivision.data.remote.response.foodlog


import com.example.nutrivision.data.remote.response.food.Serving
import com.google.gson.annotations.SerializedName

data class FoodLogDetailResponse(
    @SerializedName("calories")
    val calories: Int,
    @SerializedName("carbohydrates")
    val carbohydrates: Double,
    @SerializedName("fats")
    val fats: Double,
    @SerializedName("food_name")
    val foodName: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("meal_type")
    val mealType: String,
    @SerializedName("number_of_units")
    val numberOfUnits: Double,
    @SerializedName("proteins")
    val proteins: Double,
    @SerializedName("serving_id")
    val servingId: Int,
    @SerializedName("servings")
    val servings: List<Serving>
)