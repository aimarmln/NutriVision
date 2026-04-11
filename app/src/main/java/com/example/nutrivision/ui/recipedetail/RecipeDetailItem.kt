package com.example.nutrivision.ui.recipedetail

import com.example.nutrivision.data.remote.response.recipe.CommentItem
import com.example.nutrivision.data.remote.response.recipe.RecipeDetailResponse

sealed class RecipeDetailListItem {
    abstract val id: String
    // Data
    data class Recipe(val data: RecipeDetailResponse) : RecipeDetailListItem() {
        override val id = "recipe_${data.id}"
    }
    object CommentInput : RecipeDetailListItem() {
        override val id = "comment_input"
    }
    data class Comment(val comment: CommentListItem.Item) : RecipeDetailListItem() {
        override val id = "comment_${comment.data.id}"
    }
    object NoComments : RecipeDetailListItem() {
        override val id = "no_comments"
    }

    // Shimmer / loading
    object ShimmerRecipe : RecipeDetailListItem() {
        override val id = "shimmer_recipe"
    }       // Recipe detail shimmer

    object ShimmerCommentForm : RecipeDetailListItem() {
        override val id = "shimmer_comment_form"
    }  // Comment input shimmer

    object Loading : RecipeDetailListItem()   {
        override val id = "loading_footer"
    }           // Infinitescroll footer loading
}