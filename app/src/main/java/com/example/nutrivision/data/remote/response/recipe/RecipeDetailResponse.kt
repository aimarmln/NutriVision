package com.example.nutrivision.data.remote.response.recipe


import com.google.gson.annotations.SerializedName

data class RecipeDetailResponse(
    @SerializedName("calories_per_serving_kcal")
    val caloriesPerServingKcal: Int,
    @SerializedName("carbohydrate_per_serving_g")
    val carbohydratePerServingG: Double,
    @SerializedName("description")
    val description: String,
    @SerializedName("fat_per_serving_g")
    val fatPerServingG: Double,
    @SerializedName("health_category")
    val healthCategory: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("image_path")
    val imagePath: String,
    @SerializedName("ingredients")
    val ingredients: List<String>,
    @SerializedName("instructions")
    val instructions: List<String>,
    @SerializedName("positive_comment_count")
    val positiveCommentCount: Int,
    @SerializedName("protein_per_serving_g")
    val proteinPerServingG: Double,
    @SerializedName("recipe_name")
    val recipeName: String,
    @SerializedName("serving_yield")
    val servingYield: Int
)