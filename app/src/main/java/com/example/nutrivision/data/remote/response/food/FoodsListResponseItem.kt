package com.example.nutrivision.data.remote.response.food


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodsListResponseItem(
    @SerializedName("serving")
    val serving: Serving,
    @SerializedName("food_name")
    val foodName: String,
    @SerializedName("id")
    val id: Int
) : Parcelable