package com.example.nutrivision.data.remote.response.chat


import com.google.gson.annotations.SerializedName

data class ChatListResponseItem(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("role")
    val role: String
)