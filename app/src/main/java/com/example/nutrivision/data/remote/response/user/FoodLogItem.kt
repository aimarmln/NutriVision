package com.example.nutrivision.data.remote.response.user


import com.google.gson.annotations.SerializedName

data class FoodLogItem(
    @SerializedName("calories")
    val calories: Int,
    @SerializedName("food_id")
    val foodId: Int,
    @SerializedName("food_log_id")
    val foodLogId: Int,
    @SerializedName("food_name")
    val foodName: String
)