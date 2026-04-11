package com.example.nutrivision.ui.main.recipe

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.R
import com.example.nutrivision.data.remote.response.recipe.RecipesListResponseItem
import com.example.nutrivision.databinding.ItemRecipeBinding
import com.example.nutrivision.ui.recipedetail.RecipeDetailActivity
import kotlin.math.roundToInt

class RecipeAdapter :
    ListAdapter<RecipeListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val binding = ItemRecipeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            RecipesViewHolder(binding)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is RecipeListItem.Item -> {
                (holder as RecipesViewHolder).bind(item.data)
            }
            is RecipeListItem.Loading -> {
                // Nothing
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RecipeListItem.Item -> VIEW_TYPE_ITEM
            is RecipeListItem.Loading -> VIEW_TYPE_LOADING
        }
    }

    class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class RecipesViewHolder(private val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: RecipesListResponseItem) {
            val context = binding.root.context

            val imageResId = context.resources.getIdentifier(
                recipe.imagePath,
                "drawable",
                context.packageName
            )
            binding.recipeImage.setImageResource(imageResId)

            binding.recipeName.text = recipe.recipeName
            binding.recipeLikes.text = "🤍 ${recipe.positiveCommentCount}"
            binding.recipeCalories.text = "🔥 ${recipe.caloriesPerServingKcal} Kcal"
            binding.recipeCarbs.text = "C: ${recipe.carbohydratePerServingG.roundToInt()}g"
            binding.recipeProtein.text = "P: ${recipe.proteinPerServingG.roundToInt()}g"
            binding.recipeFat.text = "F: ${recipe.fatPerServingG.roundToInt()}g"

            binding.recipeHealthCategory.visibility =
                if (recipe.healthCategory == "Healthy") View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, RecipeDetailActivity::class.java)
                intent.putExtra(RecipeDetailActivity.Companion.EXTRA_ID, recipe.id)
                binding.root.context.startActivity(intent)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecipeListItem>() {
            override fun areItemsTheSame(
                oldItem: RecipeListItem,
                newItem: RecipeListItem
            ): Boolean {
                return if (oldItem is RecipeListItem.Item && newItem is RecipeListItem.Item) {
                    oldItem.data.id == newItem.data.id
                } else {
                    oldItem == newItem
                }
            }

            override fun areContentsTheSame(
                oldItem: RecipeListItem,
                newItem: RecipeListItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}