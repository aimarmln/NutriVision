package com.example.nutrivision.ui.foodlog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrivision.R
import com.example.nutrivision.data.remote.request.foodlog.UpdateFoodLogRequest
import com.example.nutrivision.data.remote.response.foodlog.FoodLogDetailResponse
import com.example.nutrivision.databinding.ActivityFoodLogDetailBinding
import com.example.nutrivision.databinding.BottomSheetServingBinding
import com.example.nutrivision.ui.food.fooddetail.ServingAdapter
import com.example.nutrivision.ui.food.fooddetail.ServingItem
import com.example.nutrivision.utils.showToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlin.io.path.Path
import kotlin.math.roundToInt

@AndroidEntryPoint
class FoodLogActivity: AppCompatActivity() {

    private lateinit var binding: ActivityFoodLogDetailBinding

    companion object {
        const val EXTRA_FOOD_LOG_ID = "extra_food_log_id"
    }

    private lateinit var foodLogId : String
    private lateinit var servingId : String
    private var currentFoodLog : FoodLogDetailResponse? = null

    private val foodLogViewModel: FoodLogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityFoodLogDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        foodLogId = requireNotNull(intent.getStringExtra(EXTRA_FOOD_LOG_ID))

        setupToolbar()
        setupClickListeners()
        setupNumberOfUnitsEditText()
        observeViewModel()

        foodLogViewModel.fetchFoodLogDetail(foodLogId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupClickListeners() {
        binding.btnUpdate.setOnClickListener {
            val request = validateNumberOfUnits() ?: return@setOnClickListener
            foodLogViewModel.updateFoodLog(foodLogId, request)
        }

        binding.btnDelete.setOnClickListener {
            foodLogViewModel.deleteFoodLog(foodLogId)
        }

        binding.servingUnit.setOnClickListener {
            currentFoodLog?.let {
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
                    foodLogViewModel.setNumOfUnits(numOfUnits)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        foodLogViewModel.uiState.observe(this) { state ->
            when (state) {
                is FoodLogUiState.Idle -> { }
                is FoodLogUiState.Loading -> {
                    setInitialLoading(isLoading = true)
                }
                is FoodLogUiState.Success -> {
                    setInitialLoading(isLoading = false)
                    bindFoodLogDetail(state.data)
                }
                is FoodLogUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showToast(this, state.message)
                }
            }
        }

        foodLogViewModel.updateState.observe(this) { state ->
            val isInitialSuccess = foodLogViewModel.uiState.value is FoodLogUiState.Success

            when (state) {
                is UpdateFoodLogUiState.Idle -> {
                    binding.iconCheck.visibility = View.GONE
                    binding.updateLoading.visibility = View.GONE

                    if (isInitialSuccess) {
                        binding.btnUpdate.apply {
                            isEnabled = true
                            visibility = View.VISIBLE
                        }
                    }
                }
                is UpdateFoodLogUiState.Loading -> {
                    binding.btnUpdate.apply {
                        isEnabled = false
                        visibility = View.INVISIBLE
                    }

                    setOtherButtonDim(isUpdateLoading = true, isDeleteLoading = false)

                    binding.updateLoading.visibility = View.VISIBLE
                }

                is UpdateFoodLogUiState.Success -> {
                    setOtherButtonDim(isUpdateLoading = false, isDeleteLoading = false)
                    binding.updateLoading.visibility = View.GONE
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

                is UpdateFoodLogUiState.Error -> {
                    binding.root.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    binding.updateLoading.visibility = View.GONE
                    showToast(this, state.message)
                }
            }
        }

        foodLogViewModel.deleteState.observe(this) { state ->
            val isInitialSuccess =
                foodLogViewModel.uiState.value is FoodLogUiState.Success

            when (state) {
                is DeleteFoodLogUiState.Idle -> {
                    if (isInitialSuccess) {
                        binding.btnDelete.apply {
                            isEnabled = true
                            visibility = View.VISIBLE
                        }
                    }
                }

                is DeleteFoodLogUiState.Loading -> {
                    setDeleteLoading(isLoading = true)
                    setOtherButtonDim(isUpdateLoading = false, isDeleteLoading = true)
                }

                is DeleteFoodLogUiState.Success -> {
                    setDeleteLoading(isLoading = false)
                    setOtherButtonDim(isUpdateLoading = false, isDeleteLoading = false)
                    showToast(this, state.message)
                    finish()
                }

                is DeleteFoodLogUiState.Error -> {
                    binding.root.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    setDeleteLoading(isLoading = false)
                    showToast(this, state.message)
                }
            }
        }

        foodLogViewModel.nutritionPreview.observe(this) { preview ->
            binding.foodCalories.text = preview.calories.toString()
            binding.foodCarbs.text = String.format(Locale.US, "%.1f", preview.carbs)
            binding.foodProtein.text = String.format(Locale.US, "%.1f", preview.protein)
            binding.foodFat.text = String.format(Locale.US, "%.1f", preview.fat)
        }

        foodLogViewModel.selectedServing.observe(this) { serving ->
            binding.tvServingUnit.text = serving.servingUnit
        }

        foodLogViewModel.numOfUnits.observe(this) { weight ->
            val currentText = binding.edtNumberOfUnits.text.toString()
            val newText = weight.roundToInt().toString()

            if (currentText != newText) {
                binding.edtNumberOfUnits.setText(newText)
            }
        }
    }

    private fun bindFoodLogDetail(foodLog: FoodLogDetailResponse) {
        currentFoodLog = foodLog

        binding.foodName.text = foodLog.foodName

        val serving = foodLog.servings.find { it.id == foodLog.servingId }
            ?: foodLog.servings.find { it.isDefault }
            ?: foodLog.servings.first()

        servingId = serving.id

        foodLogViewModel.initData(serving, foodLog.numberOfUnits.toFloat())
    }

    private fun showServingBottomSheet(foodLog: FoodLogDetailResponse) {
        val dialog = BottomSheetDialog(this)
        val bindingSheet = BottomSheetServingBinding.inflate(layoutInflater)
        dialog.setContentView(bindingSheet.root)

        val adapter = ServingAdapter { selectedServing ->
            servingId = selectedServing.id
            foodLogViewModel.setServing(selectedServing)

            bindingSheet.rvServing.postDelayed({
                dialog.dismiss()
            }, 250)
        }

        bindingSheet.rvServing.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@FoodLogActivity)
        }

        adapter.setSelected(servingId)

        val list = foodLog.servings.map {
            ServingItem(
                data = it,
                isSelected = it.id == servingId
            )
        }

        adapter.submitList(list)

        dialog.show()
        dialog.window?.setDimAmount(0.05f)
    }

    private fun setInitialLoading(isLoading: Boolean) {
        val viewVisibility = if (isLoading) View.GONE else View.VISIBLE
        val progressBarVisibility = if (isLoading) View.VISIBLE else View.GONE

        binding.btnUpdate.visibility = viewVisibility
        binding.btnDelete.visibility = viewVisibility
        binding.foodName.visibility = viewVisibility
        binding.materialCardView.visibility = viewVisibility
        binding.edtNumberOfUnits.visibility = viewVisibility
        binding.servingUnit.visibility = viewVisibility
        binding.progressBar.visibility = progressBarVisibility
    }

    private fun validateNumberOfUnits(): UpdateFoodLogRequest? {
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

        return UpdateFoodLogRequest(
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
                binding.edtNumberOfUnits.error = "Maxis 1000"
                false
            }
            else -> true
        }
    }

    private fun setOtherButtonDim(isUpdateLoading: Boolean, isDeleteLoading: Boolean) {
        val alpha = if (isUpdateLoading || isDeleteLoading) 0.5f else 1f

        if (!isUpdateLoading) {
            binding.btnUpdate.apply {
                isEnabled = !isDeleteLoading
                this.alpha = alpha
            }
        }

        if (!isDeleteLoading) {
            binding.btnDelete.apply {
                isEnabled = !isUpdateLoading
                this.alpha = alpha
            }
        }
    }

    private fun setDeleteLoading(isLoading: Boolean) {
        val alpha = if (isLoading) 0.5f else 1f

        binding.btnDelete.alpha = alpha
        binding.btnDelete.isEnabled = !isLoading
        binding.btnDelete.text =
            if (isLoading) "" else getString(R.string.meal_details_delete_meal)

        binding.btnLoadingDelete.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }
}