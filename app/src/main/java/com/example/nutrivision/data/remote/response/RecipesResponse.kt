package com.example.nutrivision.data.remote.response

import com.google.gson.annotations.SerializedName

data class RecipesResponse(

	@field:SerializedName("RecipesResponse")
	val recipesResponse: List<RecipesResponseItem?>? = null
)

data class RecipesResponseItem(

	@field:SerializedName("carbohydrate_per_serving_g")
	val carbohydratePerServingG: Any? = null,

	@field:SerializedName("health_category")
	val healthCategory: String? = null,

	@field:SerializedName("recipe_name")
	val recipeName: String? = null,

	@field:SerializedName("calories_per_serving_kcal")
	val caloriesPerServingKcal: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("positive_comment_count")
	val likes: Int? = null,

	@field:SerializedName("fat_per_serving_g")
	val fatPerServingG: Any? = null,

	@field:SerializedName("protein_per_serving_g")
	val proteinPerServingG: Any? = null
)
