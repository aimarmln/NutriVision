package com.example.nutrivision.ui.main.home

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.R
import com.example.nutrivision.data.remote.response.user.FoodLogSummary
import com.example.nutrivision.data.remote.response.user.FoodLogs
import com.example.nutrivision.data.remote.response.user.UserSummary
import com.example.nutrivision.databinding.FragmentHomeBinding
import com.example.nutrivision.ui.food.FoodActivity
import com.example.nutrivision.ui.food.FoodActivity.Companion.EXTRA_MEAL_TYPE
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import kotlin.math.roundToInt

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var breakfastItemMealAdapter: FoodLogAdapter
    private lateinit var lunchItemMealAdapter: FoodLogAdapter
    private lateinit var dinnerItemMealAdapter: FoodLogAdapter
    private lateinit var snacksItemMealAdapter: FoodLogAdapter

    private var hasAnimatedOnce = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupWindowInsets()
        setupRecyclerViews()
        setupClickListeners()
        observeUiState()

        if (homeViewModel.uiState.value == HomeUiState.Idle) {
            homeViewModel.fetchUserDailySummary()
        }

        return binding.root
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun setupRecyclerViews() {
        breakfastItemMealAdapter = setupRecyclerView(binding.rvBreakfast)
        lunchItemMealAdapter = setupRecyclerView(binding.rvLunch)
        dinnerItemMealAdapter = setupRecyclerView(binding.rvDinner)
        snacksItemMealAdapter = setupRecyclerView(binding.rvSnacks)
    }

    private fun setupRecyclerView(rv: RecyclerView): FoodLogAdapter {
        val adapter = FoodLogAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        return adapter
    }

    private fun setupClickListeners() {
        binding.btnLogBreakfast.setOnClickListener {
            navigateToFoodActivity("Breakfast")
        }

        binding.btnLogLunch.setOnClickListener {
            navigateToFoodActivity("Lunch")
        }

        binding.btnLogDinner.setOnClickListener {
            navigateToFoodActivity("Dinner")
        }

        binding.btnLogSnacks.setOnClickListener {
            navigateToFoodActivity("Snack")
        }
    }

    private fun navigateToFoodActivity(mealType: String) {
         val intent = Intent(activity, FoodActivity::class.java)
         intent.putExtra(EXTRA_MEAL_TYPE, mealType)
         startActivity(intent)
    }

    private fun observeUiState() {
        homeViewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state is HomeUiState.Loading && hasAnimatedOnce) return@observe

            when (state) {
                is HomeUiState.Idle -> {}
                is HomeUiState.Loading -> {
                    showLoading(true)
                }

                is HomeUiState.Success -> {
                    showLoading(false)

                    val data = state.data

                    bindUserSummary(data.userSummary)
                    bindFoodLogs(data.foodLogs)

                    hasAnimatedOnce = true
                }
                is HomeUiState.Error -> {
                    showToast(requireContext(), state.message)
                }
            }
        }
    }


    private fun bindUserSummary(user: UserSummary) {
        binding.tvUsername.text = user.name
        binding.tvCaloriesEaten.text = user.caloriesEaten.toString()

        val caloriesRemaining = user.caloriesLeft
        val caloriesMax = user.caloriesPerDay
        val caloriesEaten = user.caloriesEaten

        binding.caloriesProgressBar.max = caloriesMax
        setupCaloriesRemaining(caloriesRemaining)

        animateIfFirstTime(binding.caloriesProgressBar, caloriesEaten)

        bindMacroProgress(user.carbohydratesEaten, user.carbohydratesPerDay, binding.tvCarbs, binding.carbsProgressBar)
        bindMacroProgress(user.proteinsEaten, user.proteinsPerDay, binding.tvProtein, binding.proteinProgressBar)
        bindMacroProgress(user.fatsEaten, user.fatsPerDay, binding.tvFat, binding.fatProgressBar)
    }

    private fun setupCaloriesRemaining(caloriesRemaining: Int) {
        if (caloriesRemaining < 0) {
            val caloriesSurplus = abs(caloriesRemaining)
            binding.caloriesRemaining.text = caloriesSurplus.toString()
            binding.caloriesRemainingText.text = "Kcal over"

            val redColor = ContextCompat.getColor(requireContext(), R.color.dark_magenta)
            binding.caloriesRemaining.setTextColor(redColor)

            setProgressBar(true)
        } else {
            binding.caloriesRemaining.text = caloriesRemaining.toString()
            binding.caloriesRemainingText.text = "Kcal left"

            val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
            binding.caloriesRemaining.setTextColor(whiteColor)

            setProgressBar(false)
        }
    }

    private fun bindMacroProgress(eaten: Double, perDay: Double, textView: TextView, progressBar: ProgressBar) {
        val eatenInt = eaten.roundToInt()
        val perDayInt = perDay.roundToInt()

        textView.text = "$eatenInt / ${perDayInt}g"
        progressBar.max = perDayInt
        animateIfFirstTime(progressBar, eatenInt)
    }

    private fun bindFoodLogs(foodLogs: FoodLogs) {
        bindMeal(
            binding.tvBreakfastTitle,
            binding.rvBreakfast,
            breakfastItemMealAdapter,
            foodLogs.breakfast,
            "🥐 Breakfast")

        bindMeal(
            binding.tvLunchTitle,
            binding.rvLunch,
            lunchItemMealAdapter,
            foodLogs.lunch,
            "🍖 Lunch")

        bindMeal(
            binding.tvDinnerTitle,
            binding.rvDinner,
            dinnerItemMealAdapter,
            foodLogs.dinner,
            "🍕 Dinner")

        bindMeal(
            binding.tvSnacksTitle,
            binding.rvSnacks,
            snacksItemMealAdapter,
            foodLogs.snack,
            "🍟 Snacks")
    }

    private fun bindMeal(
        titleView: TextView,
        recyclerView: RecyclerView,
        adapter: FoodLogAdapter,
        foodLogSummary: FoodLogSummary,
        emojiTitle: String
    ) {
        val totalCalories = foodLogSummary.totalCalories
        titleView.text =
            if (totalCalories == 0) "$emojiTitle (-- Kcal)" else "$emojiTitle ($totalCalories Kcal)"

        val foods = foodLogSummary.foods

        adapter.submitList(foods)

        val marginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            if (foods.isEmpty()) 0f else 8f,
            binding.root.resources.displayMetrics
        ).toInt()

        recyclerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = marginPx
        }
    }

    private fun animateIfFirstTime(progressBar: ProgressBar, target: Int) {
        if (!hasAnimatedOnce) {
            animateProgressBar(progressBar, target)
        } else {
            progressBar.progress = target
        }
    }

    private fun animateProgressBar(progressBar: ProgressBar, targetProgress: Int, duration: Long = 1000L) {
        ObjectAnimator.ofInt(progressBar, "progress", 0, targetProgress).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun setProgressBar(isFull: Boolean) {
        val layerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_progress_bar)?.mutate()
        if (layerDrawable is LayerDrawable) {
            val progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress)
            val color = ContextCompat.getColor(requireContext(), if (isFull) R.color.dark_magenta else R.color.purple_gradient_start)
            progressDrawable?.mutate()?.setTint(color)
            binding.caloriesProgressBar.progressDrawable = layerDrawable
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.shimmerView.root.isVisible = isLoading
        binding.contentView.isVisible = !isLoading

        if (isLoading) {
            binding.shimmerView.shimmerLayout.startShimmer()
        } else {
            binding.shimmerView.shimmerLayout.stopShimmer()
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.fetchUserDailySummary()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}