package com.example.nutrivision.ui.splashscreen

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View.VISIBLE
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.MainActivity
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.databinding.ActivitySplashBinding
import com.example.nutrivision.ui.recipes.RecipesViewModel
import com.example.nutrivision.ui.recipes.RecipesViewModelFactory
import com.example.nutrivision.ui.welcome.WelcomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private val recipesViewModel: RecipesViewModel by viewModels {
        RecipesViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val pref = SettingPreferences.getInstance(application.dataStore)

        lifecycleScope.launch {
            delay(2000)
            binding.progressBar.visibility = VISIBLE
            animateProgress(0, 70, 1000) {
                recipesViewModel.fetchRecipes()
            }
        }

        recipesViewModel.recipesData.observe(this) { listRecipes ->
            if (listRecipes != null) {
                lifecycleScope.launch {
                    animateProgress(70, 100, 800) {
                        lifecycleScope.launch {
                            val isLoggedIn = pref.isLoggedIn.first()
                            val intent = if (isLoggedIn) {
                                Intent(this@SplashScreenActivity, MainActivity::class.java)
                            } else {
                                Intent(this@SplashScreenActivity, WelcomeActivity::class.java)
                            }
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun animateProgress(from: Int, to: Int, duration: Long, onEnd: () -> Unit = {}) {
        val animator = ValueAnimator.ofInt(from, to).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                binding.progressBar.progress = it.animatedValue as Int
            }
            doOnEnd { onEnd() }
        }
        animator.start()
    }

}