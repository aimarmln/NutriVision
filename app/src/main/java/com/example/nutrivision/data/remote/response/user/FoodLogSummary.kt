package com.example.nutrivision.data.remote.response.user


import com.google.gson.annotations.SerializedName

data class FoodLogSummary(
    @SerializedName("foods")
    val foods: List<FoodLogItem>,
    @SerializedName("total_calories")
    val totalCalories: Int
)