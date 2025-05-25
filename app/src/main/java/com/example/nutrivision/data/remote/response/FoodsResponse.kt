package com.example.nutrivision.data.remote.response

import com.google.gson.annotations.SerializedName

data class FoodsResponse(

	@field:SerializedName("FoodsResponse")
	val foodsResponse: List<FoodsResponseItem?>? = null
)

data class FoodsResponseItem(

	@field:SerializedName("carbohydrates_g")
	val carbohydratesG: Any? = null,

	@field:SerializedName("food_name")
	val foodName: String? = null,

	@field:SerializedName("fats_g")
	val fatsG: Any? = null,

	@field:SerializedName("weight")
	val weight: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("proteins_g")
	val proteinsG: Any? = null,

	@field:SerializedName("calories_kcal")
	val caloriesKcal: Int? = null
)
