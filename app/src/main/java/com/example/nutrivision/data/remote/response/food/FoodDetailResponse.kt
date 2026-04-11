package com.example.nutrivision.data.remote.response.food


import com.google.gson.annotations.SerializedName

data class FoodDetailResponse(
    @SerializedName("food_category")
    val foodCategory: String,
    @SerializedName("food_name")
    val foodName: String,
    @SerializedName("food_subcategory")
    val foodSubcategory: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("servings")
    val servings: List<Serving>
)