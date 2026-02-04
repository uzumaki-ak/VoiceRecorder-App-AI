package com.voicevault.recorder.utils

import android.content.Context
import android.os.Environment
import java.io.File

// handles all file operations - creating folders, getting paths, etc
object FileUtils {

    // gets the main recordings directory based on storage preference
    fun getRecordingsDirectory(context: Context, useSDCard: Boolean = false): File {
        val baseDir = if (useSDCard && hasSDCard()) {
            // try to get sd card path, fallback to internal if not available
            context.getExternalFilesDirs(null).getOrNull(1) ?: context.getExternalFilesDir(null)
        } else {
            context.getExternalFilesDir(null)
        }

        val recordingsDir = File(baseDir, Constants.RECORDINGS_FOLDER)
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }
        return recordingsDir
    }

    // temp folder for recordings in progress
    fun getTempDirectory(context: Context): File {
        val tempDir = File(context.getExternalFilesDir(null), Constants.TEMP_RECORDINGS_FOLDER)
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        return tempDir
    }

    // checks if device has sd card slot
    fun hasSDCard(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    // creates a unique file for new recording
    fun createRecordingFile(context: Context, fileName: String, useSDCard: Boolean = false): File {
        val dir = getRecordingsDirectory(context, useSDCard)
        return File(dir, "$fileName.${Constants.AUDIO_FORMAT}")
    }

    // deletes old files from recycle bin after retention period
    fun cleanupRecycleBin(context: Context) {
        val recordingsDir = getRecordingsDirectory(context)
        val cutoffTime = System.currentTimeMillis() - (Constants.RECYCLE_BIN_DAYS * 24 * 60 * 60 * 1000)

        recordingsDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                file.delete()
            }
        }
    }

    // gets file size in bytes
    fun getFileSize(filePath: String): Long {
        return File(filePath).length()
    }

    // checks if file exists
    fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }

    // deletes a file
    fun deleteFile(filePath: String): Boolean {
        return File(filePath).delete()
    }
}