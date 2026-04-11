package com.example.nutrivision.ui.auth.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.nutrivision.R
import com.example.nutrivision.databinding.FragmentWeightBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class WeightFragment : Fragment(R.layout.fragment_weight), StepFragment {

    private var _binding: FragmentWeightBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWeightBinding.bind(view)

        setupAnimation()
        restoreState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun isValid(): Boolean {
        val input = binding.edtWeight.text.toString().trim()

        if (input.isEmpty()) {
            binding.edtWeight.error = "Weight cannot be empty"
            binding.edtWeight.requestFocus()
            return false
        }

        val weight = input.toIntOrNull()

        if (weight == null || weight < 40) {
            binding.edtWeight.error = "Weight must be greater than 40 kg"
            binding.edtWeight.requestFocus()
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
        registerViewModel.formState.weightKg?.let {
            binding.edtWeight.setText(it.toString())
        }
    }

    private fun setupAnimation() {
        val shouldAnimate = arguments?.getBoolean("animate", false) ?: false
        if (shouldAnimate) {
            startEntryAnimation()
        }
    }

    fun getWeight(): Int = binding.edtWeight.text.toString().toInt()
}