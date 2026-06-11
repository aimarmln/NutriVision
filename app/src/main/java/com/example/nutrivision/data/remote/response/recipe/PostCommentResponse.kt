package com.example.nutrivision.data.remote.response.recipe


import com.google.gson.annotations.SerializedName

data class PostCommentResponse(
    @SerializedName("comment")
    val comment: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("recipe_id")
    val recipeId: Int,
    @SerializedName("sentiment")
    val sentiment: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_name")
    val userName: String
)