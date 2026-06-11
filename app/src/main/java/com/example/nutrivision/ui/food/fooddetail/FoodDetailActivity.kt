package com.example.nutrivision.ui.food.fooddetail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrivision.data.remote.request.foodlog.LogFoodRequest
import com.example.nutrivision.data.remote.response.food.FoodDetailResponse
import com.example.nutrivision.databinding.ActivityFoodDetailBinding
import com.example.nutrivision.databinding.BottomSheetServingBinding
import com.example.nutrivision.ui.food.FoodActivity.Companion.EXTRA_MEAL_TYPE
import com.example.nutrivision.utils.showToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlin.math.roundToInt

@AndroidEntryPoint
class FoodDetailActivity: AppCompatActivity() {

    companion object {
        const val EXTRA_FOOD_ID = "extra_food_id"
        const val EXTRA_SERVING_ID = "extra_serving_id"
        const val EXTRA_NUMBER_OF_UNITS = "extra_number_of_units"
    }

    private lateinit var binding: ActivityFoodDetailBinding

    private val foodDetailViewModel: FoodDetailViewModel by viewModels()

    private var foodId: Int = 0
    private lateinit var mealType: String
    private var servingId: Int = 0
    private var numberOfUnits: Float = 0f

    private var currentFood: FoodDetailResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getDataFromIntent()
        setupToolbar()
        setupClickListeners()
        setupNumberOfUnitsEditText()
        observeViewModel()

        foodDetailViewModel.fetchFoodDetail(foodId)
    }

    private fun getDataFromIntent() {
        foodId = intent.getIntExtra(EXTRA_FOOD_ID, 0)
        mealType = requireNotNull(intent.getStringExtra(EXTRA_MEAL_TYPE))
        servingId = intent.getIntExtra(EXTRA_SERVING_ID, 0)
        numberOfUnits = intent.getFloatExtra(EXTRA_NUMBER_OF_UNITS, 0f)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun observeViewModel() {
        foodDetailViewModel.uiState.observe(this) { state ->
            when (state) {
                is FoodDetailUiState.Idle -> { }
                is FoodDetailUiState.Loading -> {
                    setInitialLoading(isLoading = true)
                }
                is FoodDetailUiState.Success -> {
                    setInitialLoading(isLoading = false)
                    bindFoodDetail(state.data)
                }
                is FoodDetailUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showToast(this, state.message)
                }
            }
        }

        foodDetailViewModel.logFoodState.observe(this) { state ->
            val isInitialSuccess =
                foodDetailViewModel.uiState.value is FoodDetailUiState.Success

            when (state) {
                is LogFoodUiState.Idle -> {
                    binding.iconCheck.visibility = View.GONE
                    binding.logLoading.visibility = View.GONE

                    if (isInitialSuccess) {
                        binding.logButton.apply {
                            isEnabled = true
                            visibility = View.VISIBLE
                        }
                    }
                }
                is LogFoodUiState.Loading -> {
                    binding.logButton.apply {
                        isEnabled = false
                        visibility = View.INVISIBLE
                    }

                    binding.logLoading.visibility = View.VISIBLE
                }

                is LogFoodUiState.Success -> {
                    binding.logLoading.visibility = View.GONE
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

                is LogFoodUiState.Error -> {
                    binding.root.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    binding.logLoading.visibility = View.GONE
                    showToast(this, state.message)
                }
            }
        }

        foodDetailViewModel.nutritionPreview.observe(this) { preview ->
            binding.foodCalories.text = preview.calories.toString()
            binding.foodCarbs.text = String.format(Locale.US, "%.1f", preview.carbs)
            binding.foodProtein.text = String.format(Locale.US, "%.1f", preview.protein)
            binding.foodFat.text = String.format(Locale.US, "%.1f", preview.fat)
        }

        foodDetailViewModel.selectedServing.observe(this) { serving ->
            binding.tvServingUnit.text = serving.servingUnit
        }

        foodDetailViewModel.currentNumOfUnits.observe(this) { weight ->
            val currentText = binding.edtNumberOfUnits.text.toString()
            val newText = weight.roundToInt().toString()

            if (currentText != newText) {
                binding.edtNumberOfUnits.setText(newText)
            }
        }
    }

    private fun setupClickListeners() {
        binding.logButton.setOnClickListener {
            val request = validateNumberOfUnits() ?: return@setOnClickListener
            foodDetailViewModel.logFood(request)
        }

        binding.servingUnit.setOnClickListener {
            currentFood?.let {
                showServingBottomSheet(it)
            }
        }
    }

    private fun setupNumberOfUnitsEditText() {
        binding.edtNumberOfUnits.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val numOfUnits = s.toString().toFloatOrNull()
                if (validateText(numOfUnits) && numOfUnits != null) {
                    foodDetailViewModel.setNumOfUnits(numOfUnits)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun bindFoodDetail(food: FoodDetailResponse) {
        currentFood = food

        binding.foodName.text = food.foodName

        val serving = food.servings.find { it.id == servingId }
            ?: food.servings.find { it.isDefault }
            ?: food.servings.first()

        foodDetailViewModel.initData(serving, numberOfUnits)
    }

    private fun setInitialLoading(isLoading: Boolean) {
        val viewVisibility = if (isLoading) View.GONE else View.VISIBLE
        val progressBarVisibility = if (isLoading) View.VISIBLE else View.GONE

        binding.logButton.visibility = viewVisibility
        binding.foodName.visibility = viewVisibility
        binding.materialCardView.visibility = viewVisibility
        binding.edtNumberOfUnits.visibility = viewVisibility
        binding.servingUnit.visibility = viewVisibility
        binding.progressBar.visibility = progressBarVisibility
    }

    private fun showServingBottomSheet(food: FoodDetailResponse) {
        val dialog = BottomSheetDialog(this)
        val bindingSheet = BottomSheetServingBinding.inflate(layoutInflater)
        dialog.setContentView(bindingSheet.root)


        val adapter = ServingAdapter { selectedServing ->
            servingId = selectedServing.id
            foodDetailViewModel.setServing(selectedServing)

            bindingSheet.rvServing.postDelayed({
                dialog.dismiss()
            }, 250)
        }

        bindingSheet.rvServing.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@FoodDetailActivity)
        }

        adapter.setSelected(servingId)

        val list = food.servings.map {
            ServingItem(
                data = it,
                isSelected = it.id == servingId
            )
        }

        adapter.submitList(list)

        dialog.show()
        dialog.window?.setDimAmount(0.05f)
    }

    private fun validateNumberOfUnits(): LogFoodRequest? {
        val text = binding.edtNumberOfUnits.text.toString()

        if (text.isBlank()) {
            binding.edtNumberOfUnits.error = "Number of units cannot be empty"
            return null
        }

        val value = text.toFloatOrNull()
        if (value == null || value <= 0 || value > 1000) {
            binding.edtNumberOfUnits.error = "Please enter valid number"
            return null
        }

        return LogFoodRequest(
            foodId = foodId,
            mealType = mealType,
            numberOfUnits = value,
            servingId = servingId
        )
    }

    private fun validateText(numOfUnits: Float?): Boolean {
        return when {
            numOfUnits == null || numOfUnits <= 0f -> {
                binding.edtNumberOfUnits.error = "Please enter valid number"
                false
            }
            numOfUnits > 1000f -> {
                binding.edtNumberOfUnits.error = "Max is 1000"
                false
            }
            else -> true
        }
    }
}