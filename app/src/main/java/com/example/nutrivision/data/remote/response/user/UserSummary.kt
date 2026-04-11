package com.example.nutrivision.data.remote.response.user


import com.google.gson.annotations.SerializedName

data class UserSummary(
    @SerializedName("calories_eaten")
    val caloriesEaten: Int,
    @SerializedName("calories_left")
    val caloriesLeft: Int,
    @SerializedName("calories_per_day")
    val caloriesPerDay: Int,
    @SerializedName("carbohydrates_eaten")
    val carbohydratesEaten: Double,
    @SerializedName("carbohydrates_per_day")
    val carbohydratesPerDay: Double,
    @SerializedName("fats_eaten")
    val fatsEaten: Double,
    @SerializedName("fats_per_day")
    val fatsPerDay: Double,
    @SerializedName("name")
    val name: String,
    @SerializedName("proteins_eaten")
    val proteinsEaten: Double,
    @SerializedName("proteins_per_day")
    val proteinsPerDay: Double
)