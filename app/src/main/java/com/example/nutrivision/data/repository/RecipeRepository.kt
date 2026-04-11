package com.example.nutrivision.data.repository

import com.example.nutrivision.data.remote.api.RecipeService
import com.example.nutrivision.data.remote.request.recipe.PostRecipeCommentRequest
import com.example.nutrivision.utils.toResult

class RecipeRepository(
    private val recipeService: RecipeService
) {
    suspend fun getRecipesList(q: String?, page: Int, limit: Int) =
        recipeService.getRecipesList(q, page, limit).toResult()

    suspend fun getRecipeDetail(recipeId: String) =
        recipeService.getRecipeDetail(recipeId).toResult()

    suspend fun getRecipeComments(recipeId: String, lastCreatedAt: String?, limit: Int) =
        recipeService.getRecipeComments(recipeId, lastCreatedAt, limit).toResult()

    suspend fun postRecipeComment(recipeId: String, body: PostRecipeCommentRequest) =
        recipeService.postRecipeComment(recipeId, body).toResult()
}
