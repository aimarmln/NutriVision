package com.example.nutrivision.ui.signup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrivision.databinding.ActivityActivityLevelBinding
import com.example.nutrivision.utils.showToast
import com.google.android.material.chip.Chip

class ActivityLevelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActivityLevelBinding
    private var lastCheckedChipId: Int = View.NO_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityActivityLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val signupUser = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra<SignupUser>(SignupActivity.EXTRA_SIGNUP_USER, SignupUser::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<SignupUser>(SignupActivity.EXTRA_SIGNUP_USER)
        }

       setupChipGroup()

        binding.btnContinueActivityLevel.setOnClickListener {
            val selectedChipId = binding.chipGroupActivityLevel.checkedChipId
            if (selectedChipId == View.NO_ID) {
                showToast(this, "Please select one activity level")
                return@setOnClickListener
            }

            val selectedChip = findViewById<Chip>(selectedChipId)
            val activityLevel = selectedChip.text.toString()
            val selectedActivityLevel = activityLevel.replace(Regex("[^A-Za-z ]"), "").trim()

            signupUser?.activityLevel = selectedActivityLevel

            Log.d("ActivityLevelActivity", "SignupUser value:$signupUser")

            val intent = Intent(this@ActivityLevelActivity, MainGoalActivity::class.java)
            intent.putExtra(SignupActivity.EXTRA_SIGNUP_USER, signupUser)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupChipGroup() {
        val chipGroup = binding.chipGroupActivityLevel

        if (chipGroup.childCount > 0) {
            val firstChip = chipGroup.getChildAt(0) as? Chip
            firstChip?.isChecked = true
            lastCheckedChipId = firstChip?.id ?: View.NO_ID
        }

        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip ?: continue

            chip.setOnClickListener {
                if (chip.isChecked) {
                    lastCheckedChipId = chip.id
                } else {
                    if (chipGroup.checkedChipIds.size == 0 || chip.id == lastCheckedChipId) {
                        chip.isChecked = true
                    }
                }
            }
        }
    }
}