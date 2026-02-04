package com.voicevault.recorder

import android.app.Application
import com.voicevault.recorder.data.database.AppDatabase

// application class - initializes stuff that needs to live for the whole app lifecycle
class VoiceVaultApp : Application() {

    // lazy init means it only creates when first accessed, saves memory
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        // can add crash reporting or analytics init here later if needed
    }
}