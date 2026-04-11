package com.example.nutrivision.ui.auth.register

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.nutrivision.R
import com.example.nutrivision.databinding.FragmentNameBinding
import com.example.nutrivision.ui.auth.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NameFragment : Fragment(R.layout.fragment_name), StepFragment {

    private var _binding: FragmentNameBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNameBinding.bind(view)

        setupAnimation()
        restoreState()
        setupClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun isValid(): Boolean {
        val name = binding.edtName.text.toString().trim()

        if (name.isEmpty()) {
            binding.edtName.error = "Name cannot be empty"
            binding.edtName.requestFocus()
            return false
        }

        if (name.length < 3) {
            binding.edtName.error = "Name must be at least 3 characters"
            binding.edtName.requestFocus()
            return false
        }

        if (!name.any { it.isLetter() }) {
            binding.edtName.error = "Name must contain letters"
            binding.edtName.requestFocus()
            return false
        }

        return true
    }

    override fun startEntryAnimation() {
        val views = listOf(
            binding.tvQuestion,
            binding.edtName,
            binding.loginCtaContainer,
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

    override fun restoreState() {
        registerViewModel.formState.name?.let {
            binding.edtName.setText(it)
        }
    }

    private fun setupAnimation() {
        val shouldAnimate = arguments?.getBoolean("animate", false) ?: false
        if (shouldAnimate) {
            startEntryAnimation()
        }
    }

    private fun setupClickListeners() {
        binding.tvLoginLink.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            requireActivity().finish()
        }
    }

    fun getName(): String = binding.edtName.text.toString().trim()
}