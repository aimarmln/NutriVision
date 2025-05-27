package com.example.nutrivision.ui.scanmeal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.databinding.ActivityScanMealBinding
import com.example.nutrivision.ui.home.HomeFragment
import com.example.nutrivision.ui.scanresults.ScanResultsActivity
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanMealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanMealBinding

    private val scanMealViewModel: ScanMealViewModel by viewModels {
        ScanMealViewModelFactory(application)
    }

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private val REQUEST_CAMERA_PERMISSION = 10

    private var imageFilePath: String? = null

    private val gson = Gson()

    companion object {
        const val SCAN_RESULTS = "SCAN_RESULTS"
        const val IMAGE_PATH = "IMAGE_PATH"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanMealBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val pref = SettingPreferences.getInstance(application.dataStore)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }

        val mealType = intent.getStringExtra(HomeFragment.EXTRA_MEAL_TYPE)

        scanMealViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) VISIBLE else GONE
        }

        scanMealViewModel.predictResponse.observe(this) { response ->
            if (response != null && imageFilePath != null) {
                val intent = Intent(this@ScanMealActivity, ScanResultsActivity::class.java)
                intent.putExtra(SCAN_RESULTS, gson.toJson(response))
                intent.putExtra(IMAGE_PATH, imageFilePath)
                intent.putExtra(HomeFragment.EXTRA_MEAL_TYPE, mealType)
                startActivity(intent)
            }
        }

        binding.btnScan.setOnClickListener {
            binding.progressBar.visibility = VISIBLE
            binding.btnScan.isEnabled = false
            freezeCameraPreview()
            takePhotoAndPredict(pref)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.e("ScanMeal", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhotoAndPredict(pref: SettingPreferences) {
        val photoFile = File.createTempFile("scan_", ".jpg", cacheDir)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("ScanMeal", "Photo capture failed: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        var bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                        val exif = ExifInterface(photoFile.absolutePath)
                        val rotation = when (exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL)
                        ) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                            else -> 0f
                        }

                        if (rotation != 0f) {
                            val matrix = Matrix()
                            matrix.postRotate(rotation)
                            bitmap = Bitmap.createBitmap(
                                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                            )
                        }

                        val size = minOf(bitmap.width, bitmap.height)
                        val xOffset = (bitmap.width - size) / 2
                        val yOffset = (bitmap.height - size) / 2
                        val croppedBitmap = Bitmap.createBitmap(
                            bitmap,
                            xOffset,
                            yOffset,
                            size,
                            size
                        )

                        val croppedFile = File.createTempFile("cropped_", ".jpg", cacheDir)
                        val outputStream = FileOutputStream(croppedFile)
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                        outputStream.close()

                        imageFilePath = croppedFile.absolutePath

                        sendToViewModel(croppedFile, pref)
                    } catch (e: Exception) {
                        Log.e("ScanMeal", "Error processing image: ${e.message}", e)
                    }
                }
            })
    }

    private fun sendToViewModel(file: File, pref: SettingPreferences) {
        lifecycleScope.launch {
            val accessToken = pref.accessToken.first() ?: ""
            val refreshToken = pref.refreshToken.first() ?: ""
            scanMealViewModel.predictFood(accessToken, refreshToken, file)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun freezeCameraPreview() {
        binding.previewView.bitmap?.let { bitmap ->
            binding.freezeFrame.setImageBitmap(bitmap)
            binding.freezeFrame.visibility = VISIBLE
            binding.previewView.visibility = INVISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Log.e("ScanMeal", "Camera permission denied")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.freezeFrame.setImageBitmap(null)
        binding.freezeFrame.visibility = GONE
        binding.previewView.visibility = VISIBLE
        binding.btnScan.isEnabled = true
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}