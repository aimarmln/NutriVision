package com.example.nutrivision.data.remote.request.chat


import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("message")
    val message: String
)