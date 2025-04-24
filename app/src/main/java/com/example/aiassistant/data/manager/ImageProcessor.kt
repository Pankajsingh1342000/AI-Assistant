package com.example.aiassistant.data.manager

import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject

class ImageProcessor @Inject constructor() {
    fun encodeImageToBase64(file: File): String {
        val inputStream : InputStream = FileInputStream(file)
        val bytes = inputStream.readBytes()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}