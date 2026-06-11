package com.example.nutrivision.data.remote.response.recipe


import com.google.gson.annotations.SerializedName

data class RecipesListResponseItem(
    @SerializedName("calories_per_serving_kcal")
    val caloriesPerServingKcal: Int,
    @SerializedName("carbohydrate_per_serving_g")
    val carbohydratePerServingG: Double,
    @SerializedName("fat_per_serving_g")
    val fatPerServingG: Double,
    @SerializedName("health_category")
    val healthCategory: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("image_path")
    val imagePath: String,
    @SerializedName("positive_comment_count")
    val positiveCommentCount: Int,
    @SerializedName("protein_per_serving_g")
    val proteinPerServingG: Double,
    @SerializedName("recipe_name")
    val recipeName: String
)