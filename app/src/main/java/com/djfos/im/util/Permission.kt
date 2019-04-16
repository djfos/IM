package com.djfos.im.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

fun hasPermission(activity: Activity): Boolean {
    return ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}

fun doPermissionRequest(activity: Activity) {
    activity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
}

fun requestPermission(activity: Activity) {
    if (hasPermission(activity)) {
        return
    }

    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        AlertDialog.Builder(activity)
                .setTitle("permission required!")
                .setMessage("need storage permission to save the photo you take.")
                .setPositiveButton("OK") { _, _ -> doPermissionRequest(activity) }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    } else {
        doPermissionRequest(activity)
    }
}