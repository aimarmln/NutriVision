package com.example.nutrivision.ui.auth.register

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.nutrivision.R
import com.example.nutrivision.databinding.FragmentPasswordBinding
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class PasswordFragment : Fragment(R.layout.fragment_password), StepFragment {

    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by activityViewModels()

    private var isPasswordVisible = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPasswordBinding.bind(view)

        registerViewModel.resetRegisterState()

        setupAnimation()
        setupClickListeners()
        restoreState()
        observeRegisterState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun isValid(): Boolean {
        val password = binding.edtPassword.text.toString().trim()

        if (password.isEmpty()) {
            binding.edtPassword.error = "Password cannot be empty"
            binding.edtPassword.requestFocus()
            return false
        }

        if (password.trim().length < 8) {
            binding.edtPassword.error = "Password must be at least 8 characters"
            binding.edtPassword.requestFocus()
            return false
        }

        return true
    }

    override fun startEntryAnimation() {
        val views = listOf(
            binding.tvQuestion,
            binding.edtPassword,
            binding.tvPasswordHint,
            binding.btnEye
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

    private fun observeRegisterState() {
        val registerActivity = activity as? RegisterActivity

        registerViewModel.registerUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterUiState.Idle -> registerActivity?.setButtonLoading(false)
                is RegisterUiState.Loading -> registerActivity?.setButtonLoading(true)
                is RegisterUiState.Success -> {
                    registerActivity?.setButtonLoading(false)
                    registerActivity?.finishRegister()
                }
                is RegisterUiState.Error -> {
                    registerActivity?.setButtonLoading(false)
                    showToast(requireContext(), state.message)
                }
            }
        }
    }

    private fun setupAnimation() {
        val shouldAnimate = arguments?.getBoolean("animate", false) ?: false
        if (shouldAnimate) {
            startEntryAnimation()
        }
    }

    private fun setupClickListeners() {
        binding.btnEye.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    override fun restoreState() {
        registerViewModel.formState.password?.let {
            binding.edtPassword.setText(it)
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        val tf = binding.edtPassword.typeface

        binding.edtPassword.inputType = if (isPasswordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        binding.edtPassword.typeface = tf

        binding.btnEye.setImageResource(if (isPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye_slash)
        binding.edtPassword.setSelection(binding.edtPassword.text.length)
    }

    fun submitRegister() {
        val password = getPassword()
        registerViewModel.updatePassword(password)
        registerViewModel.register()
    }

    fun getPassword(): String = binding.edtPassword.text.toString().trim()
}