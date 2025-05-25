package com.example.nutrivision.data.remote.response

import com.google.gson.annotations.SerializedName

data class MealDetailResponse(

	@field:SerializedName("carbohydrates")
	val carbohydrates: Float? = null,

	@field:SerializedName("food_name")
	val foodName: String? = null,

	@field:SerializedName("fats")
	val fats: Float? = null,

	@field:SerializedName("weight_grams")
	val weightGrams: Int? = null,

	@field:SerializedName("proteins")
	val proteins: Float? = null,

	@field:SerializedName("calories")
	val calories: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
