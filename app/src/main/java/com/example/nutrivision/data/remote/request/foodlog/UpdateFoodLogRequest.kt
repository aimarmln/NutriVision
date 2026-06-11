package com.example.nutrivision.data.remote.request.foodlog


import com.google.gson.annotations.SerializedName

data class UpdateFoodLogRequest(
    @SerializedName("number_of_units")
    val numberOfUnits: Float,
    @SerializedName("serving_id")
    val servingId: Int
)