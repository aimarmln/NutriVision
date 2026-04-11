package com.example.nutrivision.ui.food

import android.content.Intent
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.R
import com.example.nutrivision.data.remote.response.food.FoodsListResponseItem
import com.example.nutrivision.databinding.ItemFoodBinding
import com.example.nutrivision.ui.food.FoodActivity.Companion.EXTRA_MEAL_TYPE
import com.example.nutrivision.ui.food.fooddetail.FoodDetailActivity
import com.example.nutrivision.ui.food.fooddetail.FoodDetailActivity.Companion.EXTRA_FOOD_ID
import com.example.nutrivision.ui.food.fooddetail.FoodDetailActivity.Companion.EXTRA_NUMBER_OF_UNITS
import com.example.nutrivision.ui.food.fooddetail.FoodDetailActivity.Companion.EXTRA_SERVING_ID
import com.example.nutrivision.utils.showToast
import kotlin.math.roundToInt

class FoodAdapter(
    private val mealType: String,
    private val onLogFood: (
        foodId: String,
        mealType: String,
        servingId: String,
        numberOfUnits: Float
    ) -> Unit
) : ListAdapter<FoodListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val binding = ItemFoodBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            FoodsViewHolder(binding, mealType, onLogFood)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is FoodListItem.Item -> {
                (holder as FoodsViewHolder).bind(item)
            }
            is FoodListItem.Loading -> {
                // Nothing
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FoodListItem.Item -> VIEW_TYPE_ITEM
            is FoodListItem.Loading -> VIEW_TYPE_LOADING
        }
    }

    class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class FoodsViewHolder(
        private val binding: ItemFoodBinding,
        private val mealType: String,
        private val onLogFood: (
            foodId: String,
            mealType: String,
            servingId: String,
            numberOfUnits: Float
        ) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FoodListItem.Item) {
            val food = item.data

            binding.foodName.text = food.foodName

            val serving = food.serving

            val calories = serving.caloriesKcal
            val numberOfUnits = serving.numberOfUnits.roundToInt()
            val servingUnit = serving.servingUnit

            val foodInfo = "$calories Kcal, $numberOfUnits $servingUnit"
            binding.foodInfo.text = foodInfo

            binding.progressLoading.isVisible = item.isLogging
            binding.iconCheck.isVisible = item.isLogSuccess
            binding.logButton.isVisible = !item.isLogging && !item.isLogSuccess

            if (item.isLogError) {
                binding.root.performHapticFeedback(HapticFeedbackConstants.REJECT)
                showToast(binding.root.context, "Failed to log food")
            }

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, FoodDetailActivity::class.java)

                intent.putExtra(EXTRA_FOOD_ID, food.id)
                intent.putExtra(EXTRA_MEAL_TYPE, mealType)
                intent.putExtra(EXTRA_SERVING_ID, serving.id)
                intent.putExtra(EXTRA_NUMBER_OF_UNITS, serving.numberOfUnits.toFloat())

                binding.root.context.startActivity(intent)
            }

            binding.logButton.setOnClickListener {
                binding.root.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onLogFood(
                    food.id,
                    mealType,
                    serving.id,
                    serving.numberOfUnits.toFloat()
                )
            }

            if (item.isLogSuccess) {
                binding.root.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                binding.iconCheck.apply {
                    scaleX = 0f
                    scaleY = 0f
                    alpha = 0f
                    isVisible = true

                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(250)
                        .start()
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FoodListItem>() {
            override fun areItemsTheSame(
                oldItem: FoodListItem,
                newItem: FoodListItem
            ): Boolean {
                return if (oldItem is FoodListItem.Item && newItem is FoodListItem.Item) {
                    oldItem.data.id == newItem.data.id
                } else {
                    oldItem == newItem
                }
            }

            override fun areContentsTheSame(
                oldItem: FoodListItem,
                newItem: FoodListItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}