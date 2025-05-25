package com.example.nutrivision.data.remote.request

import com.google.gson.annotations.SerializedName

data class FoodLogRequest(

	@field:SerializedName("weight_grams")
	val weightGrams: Int? = null,

	@field:SerializedName("meal_type")
	val mealType: String? = null,

	@field:SerializedName("food_id")
	val foodId: Int? = null
)
