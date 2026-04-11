package com.example.nutrivision.data.remote.response.food


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Serving(
    @SerializedName("calories_kcal")
    val caloriesKcal: Int,
    @SerializedName("carbohydrates_g")
    val carbohydratesG: Double,
    @SerializedName("description")
    val description: String?,
    @SerializedName("fats_g")
    val fatsG: Double,
    @SerializedName("id")
    val id: String,
    @SerializedName("is_default")
    val isDefault: Boolean,
    @SerializedName("number_of_units")
    val numberOfUnits: Double,
    @SerializedName("proteins_g")
    val proteinsG: Double,
    @SerializedName("serving_unit")
    val servingUnit: String
) : Parcelable