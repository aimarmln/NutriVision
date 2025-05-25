package com.example.nutrivision.data.remote.response

import com.google.gson.annotations.SerializedName

data class FoodDetailResponse(

	@field:SerializedName("food_name")
	val foodName: String? = null,

	@field:SerializedName("fat_per_100g_g")
	val fatPer100gG: Any? = null,

	@field:SerializedName("protein_per_100g_g")
	val proteinPer100gG: Any? = null,

	@field:SerializedName("carbohydrate_per_100g_g")
	val carbohydratePer100gG: Any? = null,

	@field:SerializedName("calories_per_100g_kcal")
	val caloriesPer100gKcal: Int? = null
)
