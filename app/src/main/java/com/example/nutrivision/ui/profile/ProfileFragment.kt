package com.example.nutrivision.ui.profile

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.R
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.data.remote.request.UpdateProfileRequest
import com.example.nutrivision.databinding.FragmentProfileBinding
import com.example.nutrivision.ui.welcome.WelcomeActivity
import com.example.nutrivision.utils.showToast
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(requireActivity().application)
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.purple_gradient_start)
        val window = requireActivity().window
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false

        val pref = SettingPreferences.getInstance(requireContext().dataStore)

        lifecycleScope.launch {
            val accessToken = pref.accessToken.first() ?: "Unknown access token"
            val refreshToken = pref.refreshToken.first() ?: "Unknown refresh token"
            profileViewModel.fetchUserProfile(accessToken, refreshToken)
        }

        profileViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) VISIBLE else GONE
            binding.bmiCard.visibility = if (isLoading) INVISIBLE else VISIBLE
        }

        profileViewModel.userData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                binding.name.text = response.name

                val bmiUser = response.bmi ?: 0f

                val bmiMin = 16f
                val bmiMax = 32f
                val bmiRange = bmiMax - bmiMin

                val (statusText, bgDrawableRes) = when {
                    bmiUser < 18.5f -> "Underweight" to R.drawable.bmi_status_underweight
                    bmiUser < 23.0f -> "Normal Weight" to R.drawable.bmi_status_healthy
                    bmiUser < 25.0f -> "Overweight" to R.drawable.bmi_status_overweight
                    bmiUser < 30.0f -> "Obesity Class I" to R.drawable.bmi_status_obesity_1
                    else -> "Obesity Class II" to R.drawable.bmi_status_obesity_2
                }

                binding.bmiValue.text = bmiUser.toString()
                binding.bmiStatus.text = statusText
                binding.bmiStatus.setBackgroundResource(bgDrawableRes)
                binding.bmiBarContainer.post {
                    val barWidth = binding.bmiBarContainer.width
                    val relativePosition = (bmiUser - bmiMin) / bmiRange
                    val markerX = (barWidth * relativePosition) - (binding.bmiMarker.width / 2)
                    val finalPosition = markerX.coerceIn(0f, barWidth - binding.bmiMarker.width.toFloat())
                    binding.bmiMarker.animate()
                        .translationX(finalPosition)
                        .setDuration(800L)
                        .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                        .start()
                }

                binding.edtName.setText(response.name)
                binding.edtBirthday.setText(response.birthday)
                response.age?.let { binding.edtAge.setText(it.toString()) }
                response.heightCm?.let { binding.edtHeight.setText(it.toString()) }
                response.weightKg?.let { binding.edtWeight.setText(it.toString()) }

                val activityAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.activity_levels,
                    android.R.layout.simple_spinner_item
                )
                activityAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
                binding.spinnerActivityLevel.adapter = activityAdapter

                val previousActivityLevel = response.activityLevel
                val activityArray = resources.getStringArray(R.array.activity_levels)
                val activityIndex = activityArray.indexOf(previousActivityLevel)
                if (activityIndex >= 0) {
                    binding.spinnerActivityLevel.setSelection(activityIndex)
                }

                val goalAdapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.main_goals,
                    android.R.layout.simple_spinner_item
                )
                goalAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
                binding.spinnerMainGoal.adapter = goalAdapter

                val previousMainGoal = response.mainGoal
                val goalArray = resources.getStringArray(R.array.main_goals)
                val goalIndex = goalArray.indexOf(previousMainGoal)
                if (goalIndex >= 0) {
                    binding.spinnerMainGoal.setSelection(goalIndex)
                }
            }
        }

        binding.btnUpdateProfile.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            val birthday = binding.edtBirthday.text.toString().trim()
            val heightStr = binding.edtHeight.text.toString().trim()
            val weightStr = binding.edtWeight.text.toString().trim()
            val activityLevel = binding.spinnerActivityLevel.selectedItem.toString()
            val mainGoal = binding.spinnerMainGoal.selectedItem.toString()

            var isValid = true

            if (name.isEmpty()) {
                binding.edtName.error = "Name cannot be empty"
                isValid = false
            } else if (name.length < 3) {
                binding.edtName.error = "Name must be at least 3 characters"
                isValid = false
            } else if (name.all { it.isDigit() }) {
                binding.edtName.error = "Name cannot be all numbers"
                isValid = false
            }

            val height = heightStr.toIntOrNull()
            if (heightStr.isEmpty()) {
                binding.edtHeight.error = "Height cannot be empty"
                isValid = false
            } else if (height == null || height < 140 || height > 230) {
                binding.edtHeight.error = "Height must be between 140 and 230 cm"
                isValid = false
            }

            val weight = weightStr.toIntOrNull()
            if (weightStr.isEmpty()) {
                binding.edtWeight.error = "Weight cannot be empty"
                isValid = false
            } else if (weight == null || weight <= 40) {
                binding.edtWeight.error = "Weight must be greater than 40 kg"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            val updateRequest = UpdateProfileRequest(
                name = name,
                birthday = birthday,
                heightCm = height,
                weightKg = weight,
                activityLevel = activityLevel,
                mainGoal = mainGoal
            )

            lifecycleScope.launch {
                val accessToken = pref.accessToken.first() ?: "Unknown access token"
                val refreshToken = pref.refreshToken.first() ?: "Unknown refresh token"

                profileViewModel.updateUserProfile(
                    accessToken,
                    refreshToken,
                    updateRequest
                ) { success, message ->
                    if (success) {
                        requireActivity().runOnUiThread {
                            showToast(requireContext(), message ?: "Profile updated successfully",)
                            profileViewModel.fetchUserProfile(accessToken, refreshToken)
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            showToast(requireContext(), message ?: "Profile failed to update")
                        }
                    }
                }
            }
        }

        binding.edtBirthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->
                val selectedDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                binding.edtBirthday.setText(selectedDate)
            }, year, month, day)

            datePicker.show()
        }

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                logout(pref)
            }
        }

        return root
    }

    private suspend fun logout(pref: SettingPreferences) {
        pref.clearUser()
        showToast(requireContext(), "Logout successful!")
        val intent = Intent(requireActivity(), WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.purple_gradient_start)
        val window = requireActivity().window
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.white)
        val window = requireActivity().window
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}