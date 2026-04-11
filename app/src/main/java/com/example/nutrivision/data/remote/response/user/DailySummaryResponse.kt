package com.example.nutrivision.data.remote.response.user


import com.google.gson.annotations.SerializedName

data class DailySummaryResponse(
    @SerializedName("food_logs")
    val foodLogs: FoodLogs,
    @SerializedName("user_summary")
    val userSummary: UserSummary
)