package com.example.nutrivision.data.remote.response

import com.google.gson.annotations.SerializedName

data class NutritionCalcResponse(

	@field:SerializedName("carbohydrates_g")
	val carbohydratesG: Any? = null,

	@field:SerializedName("fats_g")
	val fatsG: Any? = null,

	@field:SerializedName("proteins_g")
	val proteinsG: Any? = null,

	@field:SerializedName("calories_kcal")
	val caloriesKcal: Int? = null
)
