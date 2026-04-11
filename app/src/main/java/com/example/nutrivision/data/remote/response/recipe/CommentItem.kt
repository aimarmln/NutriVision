package com.example.nutrivision.data.remote.response.recipe


import com.google.gson.annotations.SerializedName

data class CommentItem(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("is_own_comment")
    val isOwnComment: Boolean,
    @SerializedName("name")
    val name: String,
    @SerializedName("sentiment")
    val sentiment: String,
    @SerializedName("text")
    val text: String
)