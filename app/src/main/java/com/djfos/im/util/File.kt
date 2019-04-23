package com.djfos.im.util

import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


val rootPath = File(Environment.getExternalStorageDirectory(), "IM")

val photoPath: File = checkPath(File(rootPath, "photo"))

val savePath: File = checkPath(File(rootPath, "save"))

val thumbPath: File = checkPath(File(rootPath, "thumb"))


private fun checkPath(path: File): File {
    if (!path.exists()) {
        if (!path.mkdirs())
            throw Exception("permission required to write storage")
    }
    return path
}

fun createImageFile(path: File): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd", Locale.CHINESE).format(Date())
    return File.createTempFile(timeStamp, ".jpg", path)
}