package com.example.aiassistant.data.manager

import android.content.Context
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

class CameraManager @Inject constructor(
    private val context: Context
) {
    private var imageCapture: ImageCapture? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onError: (String) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                imageCapture = ImageCapture.Builder().build()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)

            } catch (e: Exception) {
                e.printStackTrace()
                onError("Camera start failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }


    fun captureImage(file: File, onCaptured: (File) -> Unit, onError: (String) -> Unit) {
        val capture = imageCapture ?: return onError("ImageCapture not ready")

        val output = ImageCapture.OutputFileOptions.Builder(file).build()

        capture.takePicture(
            output,
            ContextCompat.getMainExecutor(context),
            object: ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onCaptured(file)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError("Capture failed: ${exception.message}")
                }

            }
        )
    }
}