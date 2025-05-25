package com.example.nutrivision.data.remote.request

import com.google.gson.annotations.SerializedName

data class CheckEmailRequest(

	@field:SerializedName("email")
	val email: String? = null
)
