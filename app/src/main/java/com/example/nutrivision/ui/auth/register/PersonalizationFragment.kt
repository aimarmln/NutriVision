package com.example.nutrivision.ui.auth.register

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.R
import com.example.nutrivision.databinding.FragmentPersonalizationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.getValue

@AndroidEntryPoint
class PersonalizationFragment : Fragment(R.layout.fragment_personalization), StepFragment {

    private var _binding: FragmentPersonalizationBinding? = null
    private val binding get() = _binding!!

    private var progressAnimator: ValueAnimator? = null
    private val registerViewModel: RegisterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPersonalizationBinding.bind(view)

        val cachedCalories = registerViewModel.personalizedCalories
        if (cachedCalories != null) {
            showResultInstantly(cachedCalories)
        } else {
            startPersonalizationWithLoading()
        }
    }

    override fun onDestroyView() {
        registerActivity()?.setContinueButtonVisible(true)

        progressAnimator?.cancel()
        progressAnimator = null
        _binding = null
        super.onDestroyView()
    }

    override fun isValid(): Boolean = true

    override fun startEntryAnimation() {
        val views = listOf(
            binding.tvLoadingTitle,
            binding.circularProgress,
            binding.tvPercent,
            binding.tvStatus,
            binding.tvLoading
        )

        views.forEachIndexed { index, view ->
            view.translationY = 80f
            view.alpha = 0f

            view.animate()
                .translationY(0f)
                .alpha(1f)
                .setStartDelay(if (index == 0) 0 else 50)
                .setDuration(220)
                .start()
        }
    }

    override fun restoreState() { }

    private fun startResultAnimation() {
        val views = listOf(
            binding.tvResultTitle,
            binding.donutChart,
            binding.calorieResultContainer,
            binding.macrosLegendContainer,
        )

        views.forEachIndexed { index, view ->
            view.translationY = 80f
            view.alpha = 0f

            view.animate()
                .translationY(0f)
                .alpha(1f)
                .setStartDelay(if (index == 0) 0 else 50)
                .setDuration(220)
                .start()
        }
    }

    private fun startPersonalizationWithLoading() {
        registerActivity()?.setContinueButtonVisible(false)
        startEntryAnimation()

        val statuses = listOf(
            "Reviewing your data...",
            "Analyzing body metrics...",
            "Calculating Basal Metabolic Rate...",
            "Determining daily calorie goals...",
            "Formulating nutrient proportions...",
            "Balancing carbs, protein, and fat...",
            "Generating personalized recommendations..."
        )

        lifecycleScope.launch {
            binding.tvPercent.text = "0%"
            binding.tvStatus.text = statuses.first()

            launch {
                for (i in 1 until statuses.size) {
                    delay(1000)
                    binding.tvStatus.text = statuses[i]
                }
            }

            progressAnimator = ValueAnimator.ofInt(0, 100).apply {
                duration = 7000
                interpolator = AccelerateDecelerateInterpolator()

                addUpdateListener { animation ->
                    val progress = animation.animatedValue as Int

                    _binding?.let { b ->
                        b.circularProgress.progress = progress
                        b.tvPercent.text = "$progress%"
                    }
                }

                doOnEnd {
                    if (_binding != null) {
                        val fraction = progressAnimator?.animatedFraction ?: 0f
                        if (fraction >= 1f) {
                            val calories = calculateCalories()
                            registerViewModel.personalizedCalories = calories
                            showResult(calories)
                        }
                    }
                }
            }

            progressAnimator?.start()
        }
    }

    private fun showResult(calories: Int) {
        registerActivity()?.setContinueButtonVisible(true)

        binding.loadingContainer.visibility = View.GONE
        binding.resultContainer.visibility = View.VISIBLE

        binding.caloriesPerDay.text = calories.toString()
        startResultAnimation()
    }

    private fun showResultInstantly(calories: Int) {
        binding.loadingContainer.visibility = View.GONE
        binding.resultContainer.visibility = View.VISIBLE

        binding.caloriesPerDay.text = calories.toString()
    }

    private fun calculateCalories(): Int {
        val state = registerViewModel.formState

        val weight = state.weightKg ?: 0
        val height = state.heightCm ?: 0
        val age = getAgeFromBirthday(state.birthday)
        val isMale = state.gender == "Male"

        val bmr = if (isMale) {
            10 * weight + 6.25 * height - 5 * age + 5
        } else {
            10 * weight + 6.25 * height - 5 * age - 161
        }

        val activityMultiplier = when (state.activityLevel) {
            "Sedentary" -> 1.2
            "Lightly Active" -> 1.375
            "Moderately Active" -> 1.55
            "Active" -> 1.725
            "Very Active" -> 1.9
            else -> 0.0
        }

        val tdee = bmr * activityMultiplier

        return when (state.mainGoal) {
            "Lose Weight" -> (tdee - 500).toInt()
            "Gain Weight" -> (tdee + 500).toInt()
            else -> tdee.toInt()
        }
    }

    fun getAgeFromBirthday(birthday: String?): Int {
        val parts = birthday?.split("-") ?: return 0
        val birthYear = parts[0].toInt()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return currentYear - birthYear
    }

    private fun registerActivity(): RegisterActivity? {
        return activity as? RegisterActivity
    }
}