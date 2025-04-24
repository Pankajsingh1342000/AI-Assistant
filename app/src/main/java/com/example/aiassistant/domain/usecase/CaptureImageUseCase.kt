package com.example.aiassistant.domain.usecase

import com.example.aiassistant.data.manager.CameraManager
import com.example.aiassistant.data.manager.ImageProcessor
import java.io.File
import javax.inject.Inject

class CaptureImageUseCase @Inject constructor(
    private val cameraManager: CameraManager,
    private val imageProcessor: ImageProcessor
) {
    fun captureAndEncode(file: File, onResult: (String) -> Unit, onError: (String) -> Unit) {
        cameraManager.captureImage(file, onCaptured = { imageFile ->
            val encoded = imageProcessor.encodeImageToBase64(imageFile)
            onResult(encoded)
        }, onError = onError)
    }
}