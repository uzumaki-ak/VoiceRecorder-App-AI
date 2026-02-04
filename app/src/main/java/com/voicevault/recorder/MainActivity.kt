package com.voicevault.recorder

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.voicevault.recorder.navigation.NavGraph
import com.voicevault.recorder.ui.theme.VoiceVaultTheme
import com.voicevault.recorder.utils.PermissionUtils

// main activity - entry point of the app
class MainActivity : ComponentActivity() {

    // permission launcher for requesting audio and storage permissions
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // check if all permissions granted
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            // handle permission denial if needed
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // request permissions if not already granted
        if (!PermissionUtils.hasAllPermissions(this)) {
            permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
        }

        setContent {
            VoiceVaultTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}