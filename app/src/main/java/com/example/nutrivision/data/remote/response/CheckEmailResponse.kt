package com.example.nutrivision.data.remote.response

import com.google.gson.annotations.SerializedName

data class CheckEmailResponse(

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("exists")
	val exists: Boolean? = null
)
