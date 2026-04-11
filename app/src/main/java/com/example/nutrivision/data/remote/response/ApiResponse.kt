package com.example.nutrivision.data.remote.response

import com.google.gson.annotations.SerializedName

data class ApiResponse<T, P>(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("pagination")
    val pagination: P? = null
)