package com.example.nutrivision.data.remote.response.user


import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("activity_level")
    val activityLevel: String,
    @SerializedName("age")
    val age: Int,
    @SerializedName("birthday")
    val birthday: String,
    @SerializedName("bmi")
    val bmi: Double,
    @SerializedName("bmi_status")
    val bmiStatus: String,
    @SerializedName("height_cm")
    val heightCm: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("main_goal")
    val mainGoal: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("weight_kg")
    val weightKg: Int
)