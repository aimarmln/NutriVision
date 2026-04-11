package com.example.nutrivision.data.remote.request.user


import com.google.gson.annotations.SerializedName

data class CompleteUserProfileRequest(
    @SerializedName("activity_level")
    val activityLevel: String,
    @SerializedName("birthday")
    val birthday: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("height_cm")
    val heightCm: Int,
    @SerializedName("main_goal")
    val mainGoal: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("weight_kg")
    val weightKg: Int
)