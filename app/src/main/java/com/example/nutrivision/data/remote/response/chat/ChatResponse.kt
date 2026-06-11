package com.example.nutrivision.data.remote.response.chat


import com.google.gson.annotations.SerializedName

data class ChatResponse(
    @SerializedName("message")
    val message: Message,
    @SerializedName("session_id")
    val sessionId: Int
)