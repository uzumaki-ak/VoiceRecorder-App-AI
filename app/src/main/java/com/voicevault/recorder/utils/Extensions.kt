package com.voicevault.recorder.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// extension functions to make life easier - adds utility methods to existing classes

// converts milliseconds to readable time format like "01:23"
fun Long.toTimeFormat(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

// converts file size in bytes to human readable format
fun Long.toFileSizeFormat(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0

    return when {
        gb >= 1 -> String.format("%.2f GB", gb)
        mb >= 1 -> String.format("%.2f MB", mb)
        kb >= 1 -> String.format("%.2f KB", kb)
        else -> "$this B"
    }
}

// FIXED: formats date to display format
fun Long.toDateFormat(): String {
    val sdf = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

// generates unique filename with timestamp
fun generateFileName(prefix: String = "Voice"): String {
    val sdf = SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault())
    return "${prefix}_${sdf.format(Date())}"
}