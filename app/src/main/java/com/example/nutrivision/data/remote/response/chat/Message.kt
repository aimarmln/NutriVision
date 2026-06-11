package com.example.nutrivision.data.remote.response.chat


import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("content")
    val content: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int
)