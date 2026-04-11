package com.example.nutrivision.ui.auth.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.nutrivision.R
import com.example.nutrivision.databinding.FragmentBirthdayBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class BirthdayFragment : Fragment(R.layout.fragment_birthday), StepFragment {

    private var _binding: FragmentBirthdayBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBirthdayBinding.bind(view)

        setupDatePicker()
        setupAnimation()
        restoreState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun isValid(): Boolean = true

    override fun startEntryAnimation() {
        binding.tvQuestion.translationY = 80f
        binding.tvQuestion.alpha = 0f

        binding.datePickerContainer.translationY = 80f
        binding.datePickerContainer.alpha = 0f

        binding.tvQuestion.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(220)
            .start()

        binding.datePickerContainer.animate()
            .translationY(0f)
            .alpha(1f)
            .setStartDelay(50)
            .setDuration(220)
            .start()
    }

    override fun restoreState() {
        val savedBirthday = registerViewModel.formState.birthday ?: return

        val parts = savedBirthday.split("-")
        if (parts.size == 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = parts[2].toInt()

            binding.datePicker.updateDate(year, month, day)
        }
    }

    private fun setupAnimation() {
        val shouldAnimate = arguments?.getBoolean("animate", false) ?: false
        if (shouldAnimate) {
            startEntryAnimation()
        }
    }

    private fun setupDatePicker() {
        val locale = Locale("id", "ID")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)

        val calendar = Calendar.getInstance()
        calendar.set(2010, Calendar.DECEMBER, 31)

        binding.datePicker.maxDate = calendar.timeInMillis
        binding.datePicker.updateDate(2004, 0, 1)
    }

    fun getBirthday(): String {
        val datePicker = binding.datePicker

        val day = datePicker.dayOfMonth
        val month = datePicker.month + 1
        val year = datePicker.year

        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day)
    }
}