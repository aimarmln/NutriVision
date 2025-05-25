package com.example.nutrivision.data.remote.request

import com.google.gson.annotations.SerializedName

data class UpdateMealRequest(

	@field:SerializedName("weight_grams")
	val weightGrams: Int? = null
)
