package com.example.nutrivision.ui.auth.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.nutrivision.R
import com.example.nutrivision.databinding.FragmentMainGoalBinding
import com.example.nutrivision.utils.showToast
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class MainGoalFragment : Fragment(R.layout.fragment_main_goal), StepFragment {

    private var _binding: FragmentMainGoalBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainGoalBinding.bind(view)

        setupAnimation()
        restoreState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun isValid(): Boolean {
        val selectedChipId = binding.chipGroupMainGoal.checkedChipId

        if (selectedChipId == View.NO_ID) {
            showToast(requireContext(), "Please select one goal")
            return false
        }

        return true
    }

    override fun startEntryAnimation() {
        binding.tvQuestion.translationY = 80f
        binding.tvQuestion.alpha = 0f

        binding.chipGroupMainGoal.translationY = 80f
        binding.chipGroupMainGoal.alpha = 0f

        binding.tvQuestion.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(220)
            .start()

        binding.chipGroupMainGoal.animate()
            .translationY(0f)
            .alpha(1f)
            .setStartDelay(50)
            .setDuration(220)
            .start()
    }

    override fun restoreState() {
        val savedGoal = registerViewModel.formState.mainGoal ?: return

        for (i in 0 until binding.chipGroupMainGoal.childCount) {
            val chip = binding.chipGroupMainGoal.getChildAt(i) as Chip
            val cleaned = chip.text.toString()
                .replace(Regex("[^A-Za-z ]"), "")
                .trim()

            if (cleaned == savedGoal) {
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

    fun getMainGoal(): String {
        val selectedChipId = binding.chipGroupMainGoal.checkedChipId
        val selectedChip = binding.root.findViewById<Chip>(selectedChipId)
        val raw = selectedChip.text.toString()
        return raw.replace(Regex("[^A-Za-z ]"), "").trim()
    }
}