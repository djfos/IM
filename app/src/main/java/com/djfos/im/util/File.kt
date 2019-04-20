package com.djfos.im.util

import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


val photoPath: File
    get() {
        val publicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val path = File(publicPath, "im")
        checkPath(path)
        return path
    }


val savePath: File
    get() {
        val publicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val foo = File(publicPath, "im")
        val bar = File(foo, "save")
        checkPath(bar)
        return bar
    }

val thumbPath: File
    get() {
        val publicPath = Environment.getExternalStorageDirectory()
        val foo = File(publicPath, "thumb")
        checkPath(foo)
        return foo
    }

private fun checkPath(path: File) {
    if (!path.exists()) {
        if (!path.mkdirs())
            throw Exception("permission required to write storage")
    }
}

fun createImageFile(path: File): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd", Locale.CHINESE).format(Date())
    return File.createTempFile(timeStamp, ".jpg", path)
}