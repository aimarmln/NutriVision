package com.example.nutrivision.data.remote.response

import com.google.gson.annotations.SerializedName

data class RecipeDetailResponse(

	@field:SerializedName("instructions")
	val instructions: List<String?>? = null,

	@field:SerializedName("comments")
	val comments: List<CommentsItem?>? = null,

	@field:SerializedName("carbohydrate_per_serving_g")
	val carbohydratePerServingG: Any? = null,

	@field:SerializedName("serving_yield")
	val servingYield: Int? = null,

	@field:SerializedName("health_category")
	val healthCategory: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("ingredients")
	val ingredients: List<String?>? = null,

	@field:SerializedName("recipe_name")
	val recipeName: String? = null,

	@field:SerializedName("calories_per_serving_kcal")
	val caloriesPerServingKcal: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("fat_per_serving_g")
	val fatPerServingG: Any? = null,

	@field:SerializedName("protein_per_serving_g")
	val proteinPerServingG: Any? = null
)

data class CommentsItem(

	@field:SerializedName("sentiment")
	val sentiment: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("text")
	val text: String? = null
)
