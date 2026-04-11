package com.example.nutrivision.ui.auth.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.nutrivision.R
import com.example.nutrivision.databinding.FragmentActivityLevelBinding
import com.example.nutrivision.utils.showToast
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class ActivityLevelFragment : Fragment(R.layout.fragment_activity_level), StepFragment {

    private var _binding: FragmentActivityLevelBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentActivityLevelBinding.bind(view)

        setupAnimation()
        restoreState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun isValid(): Boolean {
        val selectedChipId = binding.chipGroupActivityLevel.checkedChipId

        if (selectedChipId == View.NO_ID) {
            showToast(requireContext(), "Please select one activity level")
            return false
        }

        return true
    }

    override fun startEntryAnimation() {
        binding.tvQuestion.translationY = 80f
        binding.tvQuestion.alpha = 0f

        binding.chipGroupActivityLevel.translationY = 80f
        binding.chipGroupActivityLevel.alpha = 0f

        binding.tvQuestion.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(220)
            .start()

        binding.chipGroupActivityLevel.animate()
            .translationY(0f)
            .alpha(1f)
            .setStartDelay(50)
            .setDuration(220)
            .start()
    }

    override fun restoreState() {
        val savedLevel = registerViewModel.formState.activityLevel ?: return

        for (i in 0 until binding.chipGroupActivityLevel.childCount) {
            val chip = binding.chipGroupActivityLevel.getChildAt(i) as Chip
            val cleaned = chip.text.toString()
                .replace(Regex("[^A-Za-z ]"), "")
                .trim()

            if (cleaned == savedLevel) {
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

    fun getActivityLevel(): String {
        val selectedChipId = binding.chipGroupActivityLevel.checkedChipId
        val selectedChip = binding.root.findViewById<Chip>(selectedChipId)
        val raw = selectedChip.text.toString()
        return raw.replace(Regex("[^A-Za-z ]"), "").trim()
    }
}