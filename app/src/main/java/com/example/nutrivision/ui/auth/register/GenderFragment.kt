package com.example.nutrivision.ui.auth.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.nutrivision.R
import com.example.nutrivision.databinding.FragmentGenderBinding
import com.example.nutrivision.utils.showToast
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class GenderFragment : Fragment(R.layout.fragment_gender), StepFragment {

    private var _binding: FragmentGenderBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGenderBinding.bind(view)

        setupAnimation()
        restoreState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun isValid(): Boolean {
        if (binding.chipGroupGender.checkedChipId == View.NO_ID) {
            showToast(requireContext(), "Please select a gender")
            return false
        }
        return true
    }

    override fun startEntryAnimation() {
        binding.tvQuestion.translationY = 80f
        binding.tvQuestion.alpha = 0f

        binding.chipGroupGender.translationY = 80f
        binding.chipGroupGender.alpha = 0f

        binding.tvQuestion.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(220)
            .start()

        binding.chipGroupGender.animate()
            .translationY(0f)
            .alpha(1f)
            .setStartDelay(50)
            .setDuration(220)
            .start()
    }

    override fun restoreState() {
        val savedGender = registerViewModel.formState.gender ?: return

        for (i in 0 until binding.chipGroupGender.childCount) {
            val chip = binding.chipGroupGender.getChildAt(i) as Chip
            val cleaned = chip.text.toString()
                .replace(Regex("[^A-Za-z ]"), "")
                .trim()

            if (cleaned == savedGender) {
                chip.isChecked = true
                break
            }
        }
    }

    private fun setupAnimation() {
        val shouldAnimate = arguments?.getBoolean("animate", false) ?: false
        if (shouldAnimate) {
            startEntryAnimation()
        }
    }

    fun getGender(): String {
        val selectedChipId = binding.chipGroupGender.checkedChipId
        val selectedChip = binding.root.findViewById<Chip>(selectedChipId)
        val raw = selectedChip.text.toString()
        return raw.replace(Regex("[^A-Za-z ]"), "").trim()
    }
}