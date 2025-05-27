package com.example.nutrivision.ui.recipes

import android.content.ContentValues
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.data.remote.response.RecipesResponseItem
import com.example.nutrivision.databinding.ItemRecipeBinding
import com.example.nutrivision.ui.recipedetail.RecipeDetailActivity
import com.example.nutrivision.ui.recipedetail.RecipeDetailActivity.Companion.EXTRA_ID

class RecipesAdapter : ListAdapter<RecipesResponseItem, RecipesAdapter.RecipesViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecipesAdapter.RecipesViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipesViewHolder(binding)
    }

    class RecipesViewHolder(private val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: RecipesResponseItem) {
            Log.d("RecipesAdapter", "Data binding: $recipe")

            val context = binding.root.context
            val imageResId = context.resources.getIdentifier(
                "recipe_${recipe.id}",
                "drawable",
                context.packageName
            )
            binding.recipeImage.setImageResource(imageResId)

            binding.recipeName.text = recipe.recipeName ?: "Unknown name"
            binding.recipeLikes.text = "🤍 ${recipe.likes}" ?: "Unknown likes"
            binding.recipeCalories.text = "🔥 ${recipe.caloriesPerServingKcal} Kcal" ?: "Unknown calories"

            if (recipe.healthCategory == "Healthy") {
                binding.recipeHealthCategory.visibility = VISIBLE
            } else {
                binding.recipeHealthCategory.visibility = GONE
            }

            binding.recipeCarbs.text = "🍞 ${recipe.carbohydratePerServingG}g" ?: "Unknown carbohydrates"
            binding.recipeProtein.text = "🍗 ${recipe.proteinPerServingG}g" ?: "Unknown protein"
            binding.recipeFat.text = "🥑 ${recipe.fatPerServingG}g" ?: "Unknown fat"

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, RecipeDetailActivity::class.java)
                intent.putExtra(EXTRA_ID, recipe.id)
                Log.d(ContentValues.TAG, "Recipe id: ${recipe.id}")

                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onBindViewHolder(holder: RecipesAdapter.RecipesViewHolder, position: Int) {
        val recipe = getItem(position)
        holder.bind(recipe)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecipesResponseItem>() {
            override fun areItemsTheSame(oldItem: RecipesResponseItem, newItem: RecipesResponseItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: RecipesResponseItem,
                newItem: RecipesResponseItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}