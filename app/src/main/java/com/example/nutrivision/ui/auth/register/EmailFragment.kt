package com.example.nutrivision.ui.auth.register

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.nutrivision.R
import com.example.nutrivision.databinding.FragmentEmailBinding
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class EmailFragment : Fragment(R.layout.fragment_email), StepFragment {

    private var _binding: FragmentEmailBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmailBinding.bind(view)

        registerViewModel.resetCheckEmailState()

        setupAnimation()
        restoreState()
        observeCheckEmail()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun isValid(): Boolean {
        val email = binding.edtEmail.text.toString().trim()

        if (email.isEmpty()) {
            binding.edtEmail.error = "Email cannot be empty"
            binding.edtEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Please enter a valid email"
            binding.edtEmail.requestFocus()
            return false
        }

        return true
    }

    override fun startEntryAnimation() {
        binding.tvQuestion.translationY = 80f
        binding.tvQuestion.alpha = 0f

        binding.edtEmail.translationY = 80f
        binding.edtEmail.alpha = 0f

        binding.tvQuestion.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(220)
            .start()

        binding.edtEmail.animate()
            .translationY(0f)
            .alpha(1f)
            .setStartDelay(50)
            .setDuration(220)
            .start()
    }

    override fun restoreState() {
        registerViewModel.formState.email?.let {
            binding.edtEmail.setText(it)
        }
    }

    private fun setupAnimation() {
        val shouldAnimate = arguments?.getBoolean("animate", false) ?: false
        if (shouldAnimate) {
            startEntryAnimation()
        }
    }

    private fun observeCheckEmail() {
        val registerActivity = activity as? RegisterActivity

        registerViewModel.checkEmailUiState.observe(viewLifecycleOwner) { state ->
            when(state) {
                is CheckEmailUiState.Idle -> (activity as RegisterActivity).setButtonLoading(false)
                is CheckEmailUiState.Loading -> (activity as RegisterActivity).setButtonLoading(true)
                is CheckEmailUiState.Success -> {

                    registerActivity?.apply {
                        setButtonLoading(false)
                        registerViewModel.updateEmail(getEmail())
                        goToNextStep()
                    }
                }
                is CheckEmailUiState.Error -> {
                    registerActivity?.setButtonLoading(false)

                    if (state.message == "Email already registered") {
                        binding.edtEmail.error = state.message
                        binding.edtEmail.requestFocus()
                    } else {
                        showToast(requireContext(), state.message)
                    }
                }
            }
        }
    }

    fun checkEmailBeforeContinue() {
        val email = getEmail()
        registerViewModel.checkEmail(email)
    }

    fun getEmail(): String = binding.edtEmail.text.toString().trim()
}