package com.example.nutrivision.ui.auth.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.nutrivision.R
import com.example.nutrivision.databinding.FragmentHeightBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class HeightFragment : Fragment(R.layout.fragment_height), StepFragment {

    private var _binding: FragmentHeightBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHeightBinding.bind(view)

        setupAnimation()
        restoreState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun isValid(): Boolean {
        val input = binding.edtHeight.text.toString().trim()

        if (input.isEmpty()) {
            binding.edtHeight.error = "Height cannot be empty"
            binding.edtHeight.requestFocus()
            return false
        }

        val height = input.toIntOrNull()

        if (height == null || height !in 140..230) {
            binding.edtHeight.error = "Height must be between 140 and 230 cm"
            binding.edtHeight.requestFocus()
            return false
        }

        return true
    }

    override fun startEntryAnimation() {
        binding.tvQuestion.translationY = 80f
        binding.tvQuestion.alpha = 0f

        binding.heightContainer.translationY = 80f
        binding.heightContainer.alpha = 0f

        binding.tvQuestion.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(220)
            .start()

        binding.heightContainer.animate()
            .translationY(0f)
            .alpha(1f)
            .setStartDelay(50)
            .setDuration(220)
            .start()
    }

    override fun restoreState() {
        registerViewModel.formState.heightCm?.let {
            binding.edtHeight.setText(it.toString())
        }
    }

    private fun setupAnimation() {
        val shouldAnimate = arguments?.getBoolean("animate", false) ?: false
        if (shouldAnimate) {
            startEntryAnimation()
        }
    }

    fun getHeight(): Int = binding.edtHeight.text.toString().toInt()
}