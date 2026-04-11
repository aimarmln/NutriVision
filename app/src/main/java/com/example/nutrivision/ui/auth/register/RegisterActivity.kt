package com.example.nutrivision.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.example.nutrivision.R
import com.example.nutrivision.databinding.ActivityRegisterBinding
import com.example.nutrivision.ui.main.MainActivity
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private val registerViewModel: RegisterViewModel by viewModels()

    private var currentStep = -1
    private val totalSteps = 10
    private var isForward = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupButton()
        setupSystemBack()
        loadStep(0)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            handleBack()
        }
    }

    private fun setupButton() {
        binding.btnContinue.setOnClickListener {
            val fragment = getCurrentFragment()

            if (fragment is StepFragment && fragment.isValid()) {
                when (fragment) {
                    is EmailFragment -> fragment.checkEmailBeforeContinue()
                    is PasswordFragment -> fragment.submitRegister()
                    else -> {
                        when (fragment) {
                            is NameFragment -> registerViewModel.updateName(fragment.getName())
                            is GenderFragment -> registerViewModel.updateGender(fragment.getGender())
                            is BirthdayFragment -> registerViewModel.updateBirthday(fragment.getBirthday())
                            is HeightFragment -> registerViewModel.updateHeight(fragment.getHeight())
                            is WeightFragment -> registerViewModel.updateWeight(fragment.getWeight())
                            is ActivityLevelFragment -> registerViewModel.updateActivityLevel(fragment.getActivityLevel())
                            is MainGoalFragment -> registerViewModel.updateMainGoal(fragment.getMainGoal())
                        }
                        goToNextStep()
                    }
                }
            }
        }
    }

    private fun loadStep(step: Int) {
        isForward = step > currentStep
        currentStep = step

        val fragments = listOf(
            NameFragment(),
            GenderFragment(),
            BirthdayFragment(),
            HeightFragment(),
            WeightFragment(),
            ActivityLevelFragment(),
            MainGoalFragment(),
            PersonalizationFragment(),
            EmailFragment(),
            PasswordFragment()
        )

        val fragment: Fragment = fragments[step]

        fragment.arguments = Bundle().apply {
            putBoolean("animate", isForward)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()

        updateProgress()
        updateStepText()
        updateButtonText()
    }

    private fun handleBack() {
        if (currentStep == 0) {
            finish()
            return
        }

        if (currentStep == 7) {
            registerViewModel.resetPersonalization()
        }

        loadStep(currentStep - 1)
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.fragmentContainer)
    }

    private fun updateProgress() {
        binding.progressBar.max = totalSteps
        binding.progressBar.progress = currentStep + 1
    }

    private fun updateStepText() {
        binding.tvStep.text = "${currentStep + 1}/$totalSteps"
    }

    private fun getButtonText(): String {
        return if (currentStep == totalSteps - 1) {
            getString(R.string.btn_finish)
        } else {
            getString(R.string.btn_continue)
        }
    }

    private fun updateButtonText() {
        binding.btnContinue.text = getButtonText()
    }

    private fun setupSystemBack() {
        onBackPressedDispatcher.addCallback(this) {
            handleBack()
        }
    }

    fun setContinueButtonVisible(visible: Boolean) {
        binding.btnContinue.visibility =
            if (visible) View.VISIBLE else View.INVISIBLE
    }

    fun setButtonLoading(loading: Boolean) {
        binding.btnContinue.isEnabled = !loading

        val backgroundRes = if (loading) R.drawable.bg_button_disabled else R.drawable.button_gradient
        binding.btnContinue.background = AppCompatResources.getDrawable(this, backgroundRes)

        binding.btnContinue.text = if (loading) "" else getButtonText()
        binding.btnLoading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    fun goToNextStep() {
        loadStep(currentStep + 1)
    }

    fun finishRegister() {
        showToast(this, "Login successful!")
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}