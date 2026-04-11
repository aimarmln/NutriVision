package com.example.nutrivision.data.remote.response.user


import com.google.gson.annotations.SerializedName

data class FoodLogs(
    @SerializedName("Breakfast")
    val breakfast: FoodLogSummary,
    @SerializedName("Dinner")
    val dinner: FoodLogSummary,
    @SerializedName("Lunch")
    val lunch: FoodLogSummary,
    @SerializedName("Snack")
    val snack: FoodLogSummary
)