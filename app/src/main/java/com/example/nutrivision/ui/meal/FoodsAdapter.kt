package com.example.nutrivision.ui.meal

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.data.remote.response.FoodsResponseItem
import com.example.nutrivision.databinding.ItemFoodBinding

class FoodsAdapter(
    private val mealType: String?,
    private val onItemClickListener: OnItemClickListener,
    private val onLogClickListener: OnLogClickListener
) : ListAdapter<FoodsResponseItem, FoodsAdapter.FoodsViewHolder>(DIFF_CALLBACK) {

    interface OnLogClickListener {
        fun onLogClick(foodId: Int, weightGrams: Int)
    }

    interface OnItemClickListener {
        fun onItemClick(foodId: Int, mealType: String?, weightGrams: Int?)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FoodsAdapter.FoodsViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodsViewHolder(binding, mealType, onItemClickListener, onLogClickListener)
    }

    class FoodsViewHolder(
        private val binding: ItemFoodBinding,
        private val mealType: String?,
        private val onItemClickListener: OnItemClickListener,
        private val onLogClickListener: OnLogClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(food: FoodsResponseItem) {
            Log.d("FoodsAdapter", "Data binding: $food")

            binding.foodName.text = food.foodName ?: "Unknown name"

            val calories = food.caloriesKcal ?: "Unknown calories"
            val weight = food.weight ?: "Unknown weight"
            val carbs = food.carbohydratesG ?: "Unknown carbs"
            val protein = food.proteinsG ?: "Unknown protein"
            val fat = food.fatsG ?: "Unknown fat"
            val foodInfo = "$calories Kcal . ${weight}g | C: ${carbs}g . P: ${protein}g . F: ${fat}g"
            binding.foodInfo.text = foodInfo

            binding.root.setOnClickListener {
                food.id?.let { id ->
                    val weightGrams = food.weight ?: 100
                    onItemClickListener.onItemClick(id, mealType, weightGrams)
                }
            }

            binding.logButton.setOnClickListener {
                food.id?.let { id ->
                    val weightGrams = food.weight ?: 100
                    onLogClickListener.onLogClick(id, weightGrams)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: FoodsAdapter.FoodsViewHolder, position: Int) {
        val food = getItem(position)
        holder.bind(food)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FoodsResponseItem>() {
            override fun areItemsTheSame(oldItem: FoodsResponseItem, newItem: FoodsResponseItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: FoodsResponseItem,
                newItem: FoodsResponseItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}