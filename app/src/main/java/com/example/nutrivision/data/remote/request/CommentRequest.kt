package com.example.nutrivision.data.remote.request

import com.google.gson.annotations.SerializedName

data class CommentRequest(

	@field:SerializedName("recipe_id")
	val recipeId: Int? = null,

	@field:SerializedName("text")
	val text: String? = null
)
