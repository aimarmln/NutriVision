package com.example.nutrivision.data.remote.response

import com.google.gson.annotations.SerializedName

data class MessageResponse(

	@field:SerializedName("msg")
	val msg: String? = null
)
