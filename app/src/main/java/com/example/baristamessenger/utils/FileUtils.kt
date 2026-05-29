package com.example.baristamessenger.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

// Функция копирования фото во внутреннюю память приложения
fun copyUriToInternalStorage(context: Context, uri: Uri): Uri {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "app_img_${System.currentTimeMillis()}.jpg"
        val outputFile = File(context.filesDir, fileName)

        inputStream?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(outputFile)
    } catch (e: Exception) {
        e.printStackTrace()
        uri
    }
}

// Безопасное создание файла для камеры
fun createImageFileUri(context: Context): Uri {
    val directory = File(context.cacheDir, "shared_images")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    val file = File(directory, "camera_photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}