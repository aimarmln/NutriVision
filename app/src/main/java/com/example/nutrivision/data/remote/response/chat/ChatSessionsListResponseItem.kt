package com.example.nutrivision.data.remote.response.chat


import com.google.gson.annotations.SerializedName

data class ChatSessionsListResponseItem(
    @SerializedName("last_activity_at")
    val lastActivityAt: String,
    @SerializedName("last_user_message")
    val lastUserMessage: String?,
    @SerializedName("session_id")
    val sessionId: Int
)