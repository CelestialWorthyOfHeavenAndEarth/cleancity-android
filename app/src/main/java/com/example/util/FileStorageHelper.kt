package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileStorageHelper {
    fun saveBitmap(context: Context, bitmap: Bitmap): String {
        val filename = "waste_report_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file.absolutePath
    }

    fun copyUriToInternal(context: Context, sourceUri: Uri): String {
        val filename = "waste_report_${System.currentTimeMillis()}.jpg"
        val destFile = File(context.filesDir, filename)
        context.contentResolver.openInputStream(sourceUri)?.use { input: InputStream ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        return destFile.absolutePath
    }
}
