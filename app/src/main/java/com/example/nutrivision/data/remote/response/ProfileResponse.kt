package com.example.nutrivision.data.remote.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(

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

	@field:SerializedName("bmi_status")
	val bmiStatus: String? = null,

	@field:SerializedName("main_goal")
	val mainGoal: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("age")
	val age: Int? = null,

	@field:SerializedName("bmi")
	val bmi: Float? = null
)
