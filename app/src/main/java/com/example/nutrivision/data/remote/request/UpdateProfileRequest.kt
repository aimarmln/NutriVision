package com.example.nutrivision.data.remote.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(

	@field:SerializedName("birthday")
	val birthday: String? = null,

	@field:SerializedName("activity_level")
	val activityLevel: String? = null,

	@field:SerializedName("weight_kg")
	val weightKg: Int? = null,

	@field:SerializedName("height_cm")
	val heightCm: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("main_goal")
	val mainGoal: String? = null
)
