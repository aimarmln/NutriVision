package com.example.nutrivision.data.remote.request.recipe


import com.google.gson.annotations.SerializedName

data class PostRecipeCommentRequest(
    @SerializedName("comment")
    val comment: String
)