package com.example.nutrivision.ui.main.profile

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.nutrivision.R
import com.example.nutrivision.data.remote.request.user.UpdateUserProfileRequest
import com.example.nutrivision.data.remote.response.user.UserProfileResponse
import com.example.nutrivision.databinding.FragmentProfileBinding
import com.example.nutrivision.ui.auth.welcome.WelcomeActivity
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        setupWindowInsets()
        observeUiState()
        setupListeners()

        profileViewModel.fetchUserProfile()

        return binding.root
    }
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            binding.appBar.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun observeUiState() {
        profileViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileUiState.Loading -> {
                    setViewsVisibility(isVisible = false)
                }

                is ProfileUiState.UpdateLoading -> {
                    setUpdateLoading(true)
                    setLogoutLoading(false)
                    setOtherButtonDim(isUpdateLoading = true, isLogoutLoading = false)
                }

                is ProfileUiState.LogoutLoading -> {
                    setLogoutLoading(true)
                    setUpdateLoading(false)
                    setOtherButtonDim(isUpdateLoading = false, isLogoutLoading = true)
                }

                is ProfileUiState.Success -> {
                    setViewsVisibility(isVisible = true)
                    bindProfile(state.data)
                }

                is ProfileUiState.UpdateSuccess -> {
                    setUpdateLoading(false)
                    setOtherButtonDim(isUpdateLoading = false, isLogoutLoading = false)
                    showToast(requireContext(), "Profile updated successfully")
                }

                is ProfileUiState.LogoutSuccess -> {
                    setLogoutLoading(false)
                    setOtherButtonDim(isUpdateLoading = false, isLogoutLoading = false)
                    showToast(requireContext(), "Logout successful")

                    val intent = Intent(requireActivity(), WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }

                is ProfileUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogout.visibility = View.VISIBLE

                    showToast(requireContext(), state.message)
                }

                else -> Unit
            }
        }
    }

    private fun bindProfile(data: UserProfileResponse) {
        binding.name.text = data.name

        val bmi = data.bmi.toFloat()
        val (text, bg) = getBmiStatus(bmi)

        binding.bmiValue.text = bmi.toString()
        binding.bmiStatus.text = text
        binding.bmiStatus.setBackgroundResource(bg)

        animateBmiMarker(bmi)

        binding.edtName.setText(data.name)
        binding.edtBirthday.setText(data.birthday)
        binding.edtAge.setText(data.age.toString())
        binding.edtHeight.setText(data.heightCm.toString())
        binding.edtWeight.setText(data.weightKg.toString())

        setupSpinners(data)
    }

    private fun getBmiStatus(bmi: Float): Pair<String, Int> {
        return when {
            bmi < 18.5f -> "Underweight" to R.drawable.bmi_status_underweight
            bmi < 23.0f -> "Normal Weight" to R.drawable.bmi_status_healthy
            bmi < 25.0f -> "Overweight" to R.drawable.bmi_status_overweight
            bmi < 30.0f -> "Obesity Class I" to R.drawable.bmi_status_obesity_1
            else -> "Obesity Class II" to R.drawable.bmi_status_obesity_2
        }
    }

    private fun animateBmiMarker(bmi: Float) {
        val bmiMin = 16f
        val bmiMax = 32f
        val bmiRange = bmiMax - bmiMin

        binding.bmiBarContainer.post {
            val barWidth = binding.bmiBarContainer.width
            val relativePosition = (bmi - bmiMin) / bmiRange
            val markerX = (barWidth * relativePosition) - (binding.bmiMarker.width / 2)

            val finalPosition = markerX.coerceIn(
                0f,
                barWidth - binding.bmiMarker.width.toFloat()
            )

            binding.bmiMarker.animate()
                .translationX(finalPosition)
                .setDuration(800L)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    private fun setupSpinners(data: UserProfileResponse) {
        val activityAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.activity_levels,
            android.R.layout.simple_spinner_item
        )
        activityAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerActivityLevel.adapter = activityAdapter

        val activityIndex =
            resources.getStringArray(R.array.activity_levels)
                .indexOf(data.activityLevel)
        if (activityIndex >= 0)
            binding.spinnerActivityLevel.setSelection(activityIndex)

        val goalAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.main_goals,
            android.R.layout.simple_spinner_item
        )
        goalAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerMainGoal.adapter = goalAdapter

        val goalIndex =
            resources.getStringArray(R.array.main_goals)
                .indexOf(data.mainGoal)
        if (goalIndex >= 0)
            binding.spinnerMainGoal.setSelection(goalIndex)
    }

    private fun setupListeners() {
        binding.btnUpdateProfile.setOnClickListener {
            val request = validateUserProfileData() ?: return@setOnClickListener
            profileViewModel.updateUserProfile(request)
        }

        binding.edtBirthday.setOnClickListener {
            showDatePicker()
        }

        binding.btnLogout.setOnClickListener {
            profileViewModel.logout()
        }
    }

//    private fun fetchProfileIfRequired() {
//        val state = profileViewModel.uiState.value
//
//        val isNotLoading = state !is ProfileUiState.Loading &&
//                state !is ProfileUiState.UpdateLoading &&
//                state !is ProfileUiState.LogoutLoading
//
//        val isNotSuccess = state !is ProfileUiState.Success
//
//        if (isNotLoading && isNotSuccess) {
//            profileViewModel.fetchUserProfile()
//        }
//
//        profileViewModel.fetchUserProfile()
//    }

    private fun validateUserProfileData(): UpdateUserProfileRequest? {
        val name = binding.edtName.text.toString().trim()
        val birthday = binding.edtBirthday.text.toString().trim()
        val heightStr = binding.edtHeight.text.toString().trim()
        val weightStr = binding.edtWeight.text.toString().trim()

        var isValid = true

        if (name.isEmpty()) {
            binding.edtName.error = "Name cannot be empty"
            isValid = false
        } else if (name.length < 3) {
            binding.edtName.error = "Name must be at least 3 characters"
            isValid = false
        }  else if (name.all { it.isDigit() }) {
            binding.edtName.error = "Name cannot be all numbers"
            isValid = false
        }

        val height = heightStr.toIntOrNull()
        if (height == null || height < 140 || height > 230) {
            binding.edtHeight.error = "Height must be between 140 and 230 cm"
            isValid = false
        }

        val weight = weightStr.toIntOrNull()
        if (weight == null || weight < 40) {
            binding.edtWeight.error = "Weight must be greater than 40 kg"
            isValid = false
        }

        if (!isValid) return null

        return UpdateUserProfileRequest(
            name = name,
            birthday = birthday,
            heightCm = height,
            weightKg = weight,
            activityLevel = binding.spinnerActivityLevel.selectedItem.toString(),
            mainGoal = binding.spinnerMainGoal.selectedItem.toString()
        )
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        val currentBirthday = binding.edtBirthday.text.toString()

        val parts = currentBirthday.split("-") // Format: YYYY-MM-DD
        if (parts.size == 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1 // Calendar bulan dimulai dari 0
            val day = parts[2].toInt()
            cal.set(year, month, day)
        }

        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                binding.edtBirthday.setText(
                    String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                )
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setViewsVisibility(isVisible: Boolean) {
        val progressVisibility = if (isVisible) View.GONE else View.VISIBLE
        val goneIfNotVisible = if (isVisible) View.VISIBLE else View.GONE
        val invisibleIfNotVisible = if (isVisible) View.VISIBLE else View.INVISIBLE

        binding.progressBar.visibility = progressVisibility
        binding.name.visibility = invisibleIfNotVisible
        binding.bmiCard.visibility = invisibleIfNotVisible
        binding.profileCard.visibility = goneIfNotVisible
        binding.btnUpdateProfile.visibility = goneIfNotVisible
        binding.btnLogout.visibility = goneIfNotVisible
    }

    private fun setUpdateLoading(isLoading: Boolean) {
        binding.btnUpdateProfile.isEnabled = !isLoading
        binding.btnUpdateProfile.text =
            if (isLoading) "" else getString(R.string.btn_update_profile)

        binding.btnLoadingUpdate.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setLogoutLoading(isLoading: Boolean) {
        binding.btnLogout.isEnabled = !isLoading
        binding.btnLogout.text =
            if (isLoading) "" else getString(R.string.profile_logout)

        binding.btnLoadingLogout.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setOtherButtonDim(isUpdateLoading: Boolean, isLogoutLoading: Boolean) {
        val alpha = if (isUpdateLoading || isLogoutLoading) 0.5f else 1f

        if (!isUpdateLoading) {
            binding.btnUpdateProfile.alpha = alpha
            binding.btnUpdateProfile.isEnabled = !isLogoutLoading
        }

        if (!isLogoutLoading) {
            binding.btnLogout.alpha = alpha
            binding.btnLogout.isEnabled = !isUpdateLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}