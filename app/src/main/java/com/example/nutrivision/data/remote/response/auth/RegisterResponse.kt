package com.example.nutrivision.data.remote.response.auth


import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("user")
    val user: User
)