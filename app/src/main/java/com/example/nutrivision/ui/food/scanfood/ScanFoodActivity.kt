package com.example.nutrivision.ui.food.scanfood

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
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
import com.example.nutrivision.R
import com.example.nutrivision.databinding.ActivityScanFoodBinding
import com.example.nutrivision.ui.food.FoodActivity.Companion.EXTRA_MEAL_TYPE
import com.example.nutrivision.ui.food.scanfood.scanresult.ScanResultActivity
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class ScanFoodActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SCAN_RESULTS = "extra_scan_results"
        const val EXTRA_IMAGE_PATH = "extra_image_path"
    }

    private lateinit var binding: ActivityScanFoodBinding

    private val scanFoodViewModel: ScanFoodViewModel by viewModels()

    private var isCameraStarted = false
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private val REQUEST_CAMERA_PERMISSION = 10
    private var imageFilePath: String? = null
    private lateinit var mealType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mealType = requireNotNull(intent.getStringExtra(EXTRA_MEAL_TYPE))

        setupToolbar()
        observeViewModel()
        setupClickListeners()
        setupCamera()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun observeViewModel() {
        scanFoodViewModel.uiState.observe(this) { state ->
            when (state) {
                is ScanFoodUiState.Idle -> {
                    binding.progressBar.visibility = GONE
                    binding.btnScan.isEnabled = true
                }

                is ScanFoodUiState.Loading -> {
                    binding.progressBar.visibility = VISIBLE
                    binding.btnScan.isEnabled = false
                }

                is ScanFoodUiState.Success -> {
                    val intent = Intent(this, ScanResultActivity::class.java)

                    intent.putParcelableArrayListExtra(
                        EXTRA_SCAN_RESULTS,
                        ArrayList(state.data)
                    )

                    intent.putExtra(EXTRA_IMAGE_PATH, R.drawable.nasi_ayam_bakar)
//                    intent.putExtra(EXTRA_IMAGE_PATH, imageFilePath)
                    intent.putExtra(EXTRA_MEAL_TYPE, mealType)
                    startActivity(intent)

                    scanFoodViewModel.resetState()
                }

                is ScanFoodUiState.Error -> {
                    binding.progressBar.visibility = GONE
                    binding.btnScan.isEnabled = true

                    binding.freezeFrame.visibility = GONE
                    binding.previewView.visibility = VISIBLE

                    startCamera()
                    showToast(this, state.message)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnScan.setOnClickListener {
            sendDummyImageToApi()
//            freezeCameraPreview()
//            takePhotoAndDetect()
        }
    }

    private fun setupCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun startCamera() {
        if (isCameraStarted) return
        isCameraStarted = true

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
                showToast(this, "Camera binding failed")
                Log.e("ScanMeal", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhotoAndDetect() {
        val photoFile = File.createTempFile("scan_", ".jpg", cacheDir)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    showToast(this@ScanFoodActivity, "Failed to take photo")

                    binding.progressBar.visibility = GONE
                    binding.btnScan.isEnabled = true

                    binding.freezeFrame.visibility = GONE
                    binding.previewView.visibility = VISIBLE

                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        val options = BitmapFactory.Options().apply {
                            inSampleSize = 2
                        }

                        var bitmap = BitmapFactory.decodeFile(photoFile.absolutePath, options)

                        val exif = ExifInterface(photoFile.absolutePath)
                        val rotation = when (exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
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

                        val finalPath = croppedFile.absolutePath


                        imageFilePath = finalPath
                        scanFoodViewModel.detectFoods(croppedFile)

                        bitmap.recycle()
                        croppedBitmap.recycle()

                    } catch (e: Exception) {
                        showToast(this@ScanFoodActivity, "Error processing image")

                        Log.e("ScanMeal", "Error processing image: ${e.message}", e)
                    }
                }

            })
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

    private fun drawableToFile(drawableResId: Int): File {
        val bitmap = BitmapFactory.decodeResource(resources, drawableResId)

        val file = File.createTempFile("dummy_food_", ".jpg", cacheDir)

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }

        bitmap.recycle()

        return file
    }

    private fun sendDummyImageToApi() {
        binding.progressBar.visibility = VISIBLE
        binding.btnScan.isEnabled = false

        val dummyFile = drawableToFile(R.drawable.nasi_ayam_bakar)

        imageFilePath = dummyFile.absolutePath

        scanFoodViewModel.detectFoods(dummyFile)
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

    override fun onPause() {
        super.onPause()
        isCameraStarted = false
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}