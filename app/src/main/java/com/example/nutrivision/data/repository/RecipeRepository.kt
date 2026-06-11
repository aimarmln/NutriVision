package com.example.nutrivision.data.repository

import com.example.nutrivision.data.remote.api.RecipeService
import com.example.nutrivision.data.remote.request.recipe.PostRecipeCommentRequest
import com.example.nutrivision.utils.toResult

class RecipeRepository(
    private val recipeService: RecipeService
) {
    suspend fun getRecipesList(q: String?, page: Int, limit: Int) =
        recipeService.getRecipesList(q, page, limit).toResult()

    suspend fun getRecipeDetail(recipeId: Int) =
        recipeService.getRecipeDetail(recipeId).toResult()

    suspend fun getRecipeComments(recipeId: Int, lastCreatedAt: String?, limit: Int) =
        recipeService.getRecipeComments(recipeId, lastCreatedAt, limit).toResult()

    suspend fun postRecipeComment(recipeId: Int, body: PostRecipeCommentRequest) =
        recipeService.postRecipeComment(recipeId, body).toResult()
}
