package com.example.nutrivision.ui.signup

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.R
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.databinding.ActivityPersonalizationBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PersonalizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalizationBinding

    private val signupViewModel: SignupViewModel by viewModels {
        SignupViewModelFactory()
    }

    private val statusTexts = listOf(
        "Reviewing your data...",
        "Analyzing body metrics...",
        "Calculating Basal Metabolic Rate...",
        "Determining daily calorie goals...",
        "Formulating nutrient proportions...",
        "Balancing carbs, protein, and fat...",
        "Generating personalized recommendations..."
    )

    companion object {
        const val EXTRA_CALORIES = "EXTRA_CALORIES"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPersonalizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val signupUser = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra<SignupUser>(SignupActivity.EXTRA_SIGNUP_USER, SignupUser::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<SignupUser>(SignupActivity.EXTRA_SIGNUP_USER)
        }

        val pref = SettingPreferences.getInstance(application.dataStore)

        signupViewModel.signupResponse.observe(this) { response ->
            if (response != null) {
                lifecycleScope.launch {
                    pref.saveTokens(
                        response.accessToken.orEmpty(),
                        response.refreshToken.orEmpty()
                    )
                }

                val intent = Intent(this, CaloriePlanActivity::class.java)
                intent.putExtra(EXTRA_CALORIES, response.msg)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Log.d("PersonalizationActivity", "Data is empty for response")
            }
        }

        startProgressAnimation(signupUser)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // No action
            }
        })
    }

    private fun startProgressAnimation(signupUser: SignupUser?) {
        lifecycleScope.launch {
            binding.tvPercent.text = getString(R.string.personalization_progress_percent, 0)
            binding.tvStatus.text = statusTexts[0]
            delay(500)

            launch {
                for (i in 1 until statusTexts.size) {
                    delay(1000)
                    binding.tvStatus.text = statusTexts[i]
                }
            }

            val animator = ValueAnimator.ofInt(0, 100).apply {
                duration = 7000
                interpolator = AccelerateDecelerateInterpolator()

                addUpdateListener { animation ->
                    val progress = animation.animatedValue as Int
                    binding.tvPercent.text = getString(R.string.personalization_progress_percent, progress)
                    binding.circularProgress.progress = progress
                }

                doOnEnd {
                    lifecycleScope.launch {
                        delay(500)

                        signupUser?.let {
                            signupViewModel.signUp(it)
                        }
                    }
                }
            }

            animator.start()
        }
    }
}