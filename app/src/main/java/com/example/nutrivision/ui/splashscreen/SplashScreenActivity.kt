package com.example.nutrivision.ui.splashscreen

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.MainActivity
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.databinding.ActivitySplashBinding
import com.example.nutrivision.ui.home.HomeViewModel
import com.example.nutrivision.ui.home.HomeViewModelFactory
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

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(application)
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
                    val isLoggedIn = pref.isLoggedIn.first()
                    if (isLoggedIn) {
                        animateProgress(70, 88, 800) {
                            lifecycleScope.launch {
                                val accessToken = pref.accessToken.first() ?: "Unknown access token"
                                val refreshToken = pref.refreshToken.first() ?: "Unknown refresh token"
                                homeViewModel.home(accessToken, refreshToken)
                            }
                        }
                    } else {
                        animateProgress(70, 100, 800) {
                            val intent = Intent(this@SplashScreenActivity, WelcomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            } else {
                throw IllegalStateException("Server is off. Cannot proceed.")
            }
        }

        homeViewModel.homeData.observe(this) { response ->
            Log.d("SplashScreenActivity", "User isLoggedIn response: $response")
            if (response != null) {
                animateProgress(88, 100, 600) {
                    val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                animateProgress(88, 100, 600) {
                    val intent = Intent(this@SplashScreenActivity, WelcomeActivity::class.java)
                    startActivity(intent)
                    finish()
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