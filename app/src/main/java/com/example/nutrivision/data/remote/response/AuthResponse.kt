package com.example.nutrivision.data.remote.response

import com.google.gson.annotations.SerializedName

data class AuthResponse(

	@field:SerializedName("access_token")
	val accessToken: String? = null,

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("refresh_token")
	val refreshToken: String? = null
)
