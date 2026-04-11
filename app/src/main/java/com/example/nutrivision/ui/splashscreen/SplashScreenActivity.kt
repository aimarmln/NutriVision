package com.example.nutrivision.ui.splashscreen

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View.VISIBLE
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.databinding.ActivitySplashBinding
import com.example.nutrivision.ui.auth.welcome.WelcomeActivity
import com.example.nutrivision.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val pref = SettingPreferences.getInstance(application.dataStore)

        lifecycleScope.launch {
            binding.progressBar.visibility = VISIBLE

            animateProgress(0, 100, 1500)

            val accessToken = pref.accessToken.first()

            delay(1500)

            if (!accessToken.isNullOrEmpty()) {
                goToMain()
            } else {
                goToWelcome()
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToWelcome() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }

    private fun animateProgress(from: Int, to: Int, duration: Long) {
        val animator = ValueAnimator.ofInt(from, to).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                binding.progressBar.progress = it.animatedValue as Int
            }
        }
        animator.start()
    }
}