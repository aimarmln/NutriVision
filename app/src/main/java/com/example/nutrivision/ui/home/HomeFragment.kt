package com.example.nutrivision.ui.home

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrivision.R
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.databinding.FragmentHomeBinding
import com.example.nutrivision.ui.meal.MealActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(requireActivity().application)
    }

    companion object {
        const val EXTRA_MEAL_TYPE = "EXTRA_MEAL_TYPE"
    }

    private lateinit var breakfastItemMealAdapter: ItemMealAdapter
    private lateinit var lunchItemMealAdapter: ItemMealAdapter
    private lateinit var dinnerItemMealAdapter: ItemMealAdapter
    private lateinit var snacksItemMealAdapter: ItemMealAdapter

    private var hasAnimatedOnce = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val pref = SettingPreferences.getInstance(requireContext().dataStore)
        lifecycleScope.launch {
            val accessToken = pref.accessToken.first() ?: "Unknown access token"
            val refreshToken = pref.refreshToken.first() ?: "Unknown refresh token"
            homeViewModel.home(accessToken, refreshToken)
        }

        breakfastItemMealAdapter = ItemMealAdapter()
        binding.rvBreakfast.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBreakfast.adapter = breakfastItemMealAdapter

        lunchItemMealAdapter = ItemMealAdapter()
        binding.rvLunch.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLunch.adapter = lunchItemMealAdapter

        dinnerItemMealAdapter = ItemMealAdapter()
        binding.rvDinner.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDinner.adapter = dinnerItemMealAdapter

        snacksItemMealAdapter = ItemMealAdapter()
        binding.rvSnacks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSnacks.adapter = snacksItemMealAdapter

        homeViewModel.homeData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                // Main card
                binding.tvUsername.text = response.user?.name
                binding.tvCaloriesEaten.text = response.user?.caloriesEaten.toString()

                val caloriesRemaining = response.user?.caloriesLeft ?: 0
                if (caloriesRemaining < 0) {
                    val caloriesSurplus = abs(caloriesRemaining)
                    binding.caloriesRemaining.text = caloriesSurplus.toString()
                    binding.caloriesRemainingText.text = "Kcal over"

                    val redColor = ContextCompat.getColor(requireContext(), R.color.dark_magenta)
                    binding.caloriesRemaining.setTextColor(redColor)
                    binding.caloriesProgressBar.progressDrawable.setTint(redColor)
                } else {
                    binding.caloriesRemaining.text = caloriesRemaining.toString()
                    binding.caloriesRemainingText.text = "Kcal left"

                    val normalDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_progress_bar)
                    binding.caloriesProgressBar.progressDrawable = normalDrawable

                    val defaultColor = ContextCompat.getColor(requireContext(), R.color.white)
                    binding.caloriesRemaining.setTextColor(defaultColor)
                }

                val caloriesMax = response.user?.caloriesPerDay ?: 0
                val caloriesEaten = response.user?.caloriesEaten ?: 0
                binding.caloriesProgressBar.max = caloriesMax
                if (!hasAnimatedOnce) {
                    animateProgressBar(binding.caloriesProgressBar, response.user?.caloriesEaten ?: 0)
                } else {
                    binding.caloriesProgressBar.progress = response.user?.caloriesEaten ?: 0
                }

                val carbsEaten = response.user?.carbohydratesEaten?.roundToInt()
                val carbsPerDay = response.user?.carbohydratesPerDay?.roundToInt()
                val carbsText = "$carbsEaten / ${carbsPerDay}g"
                binding.tvCarbs.text = carbsText
                binding.carbsProgressBar.max = carbsPerDay ?: 0
                if (!hasAnimatedOnce) {
                    animateProgressBar(binding.carbsProgressBar, carbsEaten ?: 0)
                } else {
                    binding.carbsProgressBar.progress = carbsEaten ?: 0
                }

                val proteinsEaten = response.user?.proteinsEaten?.roundToInt()
                val proteinsPerDay = response.user?.proteinsPerDay?.roundToInt()
                val proteinText = "$proteinsEaten / ${proteinsPerDay}g"
                binding.tvProtein.text = proteinText
                binding.proteinProgressBar.max = proteinsPerDay ?: 0
                binding.proteinProgressBar.max = proteinsPerDay ?: 0
                if (!hasAnimatedOnce) {
                    animateProgressBar(binding.proteinProgressBar, proteinsEaten ?: 0)
                } else {
                    binding.proteinProgressBar.progress = proteinsEaten ?: 0
                }

                val fatsEaten = response.user?.fatsEaten?.roundToInt()
                val fatsPerDay = response.user?.fatsPerDay?.roundToInt()
                val fatText = "$fatsEaten / ${fatsPerDay}g"
                binding.tvFat.text = fatText
                binding.fatProgressBar.max = fatsPerDay ?: 0
                if (!hasAnimatedOnce) {
                    animateProgressBar(binding.fatProgressBar, fatsEaten ?: 0)
                } else {
                    binding.fatProgressBar.progress = fatsEaten ?: 0
                }

                // Flag animated once
                hasAnimatedOnce = true

                // Meals
                val caloriesFromBreakfast = response.mealLogs?.breakfast?.totalCalories
                if (caloriesFromBreakfast == 0) {
                    binding.tvBreakfastTitle.text = "🥐 Breakfast (-- Kcal)"
                } else {
                    binding.tvBreakfastTitle.text = "🥐 Breakfast (${caloriesFromBreakfast.toString()} Kcal)"
                }

                val caloriesFromLunch = response.mealLogs?.lunch?.totalCalories
                if (caloriesFromLunch == 0) {
                    binding.tvLunchTitle.text = "🍖 Lunch (-- Kcal)"
                } else {
                    binding.tvLunchTitle.text = "🍖 Lunch (${caloriesFromLunch.toString()} Kcal)"
                }

                val caloriesFromDinner = response.mealLogs?.dinner?.totalCalories
                if (caloriesFromDinner == 0) {
                    binding.tvDinnerTitle.text = "🍕 Dinner (-- Kcal)"
                } else {
                    binding.tvDinnerTitle.text = "🍕 Dinner (${caloriesFromDinner.toString()} Kcal)"
                }

                val caloriesFromSnacks = response.mealLogs?.snacks?.totalCalories
                if (caloriesFromSnacks == 0) {
                    binding.tvSnacksTitle.text = "🍟 Snacks (-- Kcal)"
                } else {
                    binding.tvSnacksTitle.text = "🍟 Snacks (${caloriesFromSnacks.toString()} Kcal)"
                }

                // Item meals
                if (!response.mealLogs?.breakfast?.meals.isNullOrEmpty()) {
                    binding.rvBreakfast.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            8f,
                            binding.root.resources.displayMetrics
                        ).toInt()
                    }

                    val sortedMeals = response.mealLogs?.breakfast?.meals?.sortedBy { it?.mealId }
                    breakfastItemMealAdapter.submitList(sortedMeals)
                } else {
                    breakfastItemMealAdapter.submitList(emptyList())
                }

                if (!response.mealLogs?.lunch?.meals.isNullOrEmpty()) {
                    binding.rvLunch.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            8f,
                            binding.root.resources.displayMetrics
                        ).toInt()
                    }

                    val sortedMeals = response.mealLogs?.lunch?.meals?.sortedBy { it?.mealId }
                    lunchItemMealAdapter.submitList(sortedMeals)
                } else {
                    lunchItemMealAdapter.submitList(emptyList())
                }

                if (!response.mealLogs?.dinner?.meals.isNullOrEmpty()) {
                    binding.rvDinner.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            8f,
                            binding.root.resources.displayMetrics
                        ).toInt()
                    }

                    val sortedMeals = response.mealLogs?.dinner?.meals?.sortedBy { it?.mealId }
                    dinnerItemMealAdapter.submitList(sortedMeals)
                } else {
                    dinnerItemMealAdapter.submitList(emptyList())
                }

                if (!response.mealLogs?.snacks?.meals.isNullOrEmpty()) {
                    binding.rvSnacks.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            8f,
                            binding.root.resources.displayMetrics
                        ).toInt()
                    }

                    val sortedMeals = response.mealLogs?.snacks?.meals?.sortedBy { it?.mealId }
                    snacksItemMealAdapter.submitList(sortedMeals)
                } else {
                    snacksItemMealAdapter.submitList(emptyList())
                }

            }
        }

        binding.btnLogBreakfast.setOnClickListener {
            navigateToMealActivity("Breakfast")
        }

        binding.btnLogLunch.setOnClickListener {
            navigateToMealActivity("Lunch")
        }

        binding.btnLogDinner.setOnClickListener {
            navigateToMealActivity("Dinner")
        }

        binding.btnLogSnacks.setOnClickListener {
            navigateToMealActivity("Snacks")
        }

        return root
    }

    private fun navigateToMealActivity(mealType: String) {
        val intent = Intent(activity, MealActivity::class.java)
        intent.putExtra(EXTRA_MEAL_TYPE, mealType)
        startActivity(intent)
    }

    private fun animateProgressBar(progressBar: ProgressBar, targetProgress: Int, duration: Long = 1000L) {
        ObjectAnimator.ofInt(progressBar, "progress", 0, targetProgress).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val pref = SettingPreferences.getInstance(requireContext().dataStore)
            val accessToken = pref.accessToken.first() ?: "Unknown access token"
            val refreshToken = pref.refreshToken.first() ?: "Unknown refresh token"
            homeViewModel.home(accessToken, refreshToken)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}