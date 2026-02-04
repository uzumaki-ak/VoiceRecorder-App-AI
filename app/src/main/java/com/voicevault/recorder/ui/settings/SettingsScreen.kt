package com.voicevault.recorder.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicevault.recorder.utils.Constants

// settings screen for app configuration
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToApiSettings: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val recordingQuality by viewModel.recordingQuality.collectAsState()
    val blockCalls by viewModel.blockCalls.collectAsState()
    val autoPlayNext by viewModel.autoPlayNext.collectAsState()
    val useBluetooth by viewModel.useBluetooth.collectAsState()
    val storageLocation by viewModel.storageLocation.collectAsState()
    val recycleBinEnabled by viewModel.recycleBinEnabled.collectAsState()
    val saveSearches by viewModel.saveSearches.collectAsState()
    val autoTranscribe by viewModel.autoTranscribe.collectAsState()

    var showQualityDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Recorder settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // recording quality
            ListItem(
                headlineContent = { Text("Recording quality") },
                supportingContent = { Text(recordingQuality.displayName) },
                modifier = Modifier.clickable { showQualityDialog = true }
            )

            Divider()

            // block calls while recording
            ListItem(
                headlineContent = { Text("Block calls while recording") },
                trailingContent = {
                    Switch(
                        checked = blockCalls,
                        onCheckedChange = { viewModel.setBlockCalls(it) }
                    )
                }
            )

            Divider()

            // auto play next recording
            ListItem(
                headlineContent = { Text("Auto play next recording") },
                trailingContent = {
                    Switch(
                        checked = autoPlayNext,
                        onCheckedChange = { viewModel.setAutoPlayNext(it) }
                    )
                }
            )

            Divider()

            // use bluetooth mic
            ListItem(
                headlineContent = { Text("Use Bluetooth mic when available") },
                trailingContent = {
                    Switch(
                        checked = useBluetooth,
                        onCheckedChange = { viewModel.setUseBluetooth(it) }
                    )
                }
            )

            Divider()

            // storage location
            ListItem(
                headlineContent = { Text("Storage location") },
                supportingContent = {
                    Text(if (storageLocation == Constants.STORAGE_INTERNAL) "Internal" else "SD card")
                },
                modifier = Modifier.clickable { showStorageDialog = true }
            )

            Divider()

            // recycle bin
            ListItem(
                headlineContent = { Text("Recycle bin") },
                supportingContent = { Text("Keep deleted recordings for 30 days.") },
                trailingContent = {
                    Switch(
                        checked = recycleBinEnabled,
                        onCheckedChange = { viewModel.setRecycleBinEnabled(it) }
                    )
                }
            )

            Divider()

            // save recent searches
            ListItem(
                headlineContent = { Text("Save recent searches") },
                trailingContent = {
                    Switch(
                        checked = saveSearches,
                        onCheckedChange = { viewModel.setSaveSearches(it) }
                    )
                }
            )

            Divider()

            // auto transcribe
            ListItem(
                headlineContent = { Text("Auto transcribe recordings") },
                supportingContent = { Text("Automatically transcribe recordings after saving") },
                trailingContent = {
                    Switch(
                        checked = autoTranscribe,
                        onCheckedChange = { viewModel.setAutoTranscribe(it) }
                    )
                }
            )

            Divider()

            // api settings
            ListItem(
                headlineContent = { Text("AI API Settings") },
                supportingContent = { Text("Configure LLM providers and API keys") },
                modifier = Modifier.clickable { onNavigateToApiSettings() }
            )

            Divider()

            Spacer(modifier = Modifier.weight(1f))

            // about section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 1.dp
            ) {
                ListItem(
                    headlineContent = { Text("About Voice Recorder") },
                    modifier = Modifier.clickable { /* show about dialog */ }
                )
            }
        }
    }

    // quality selection dialog
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Recording quality") },
            text = {
                Column {
                    Constants.RecordingQuality.values().forEach { quality ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setRecordingQuality(quality)
                                    showQualityDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = quality == recordingQuality,
                                onClick = {
                                    viewModel.setRecordingQuality(quality)
                                    showQualityDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(quality.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // storage location dialog
    if (showStorageDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            title = { Text("Storage location") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setStorageLocation(Constants.STORAGE_INTERNAL)
                                showStorageDialog = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = storageLocation == Constants.STORAGE_INTERNAL,
                            onClick = {
                                viewModel.setStorageLocation(Constants.STORAGE_INTERNAL)
                                showStorageDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Internal storage")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setStorageLocation(Constants.STORAGE_SD_CARD)
                                showStorageDialog = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = storageLocation == Constants.STORAGE_SD_CARD,
                            onClick = {
                                viewModel.setStorageLocation(Constants.STORAGE_SD_CARD)
                                showStorageDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SD card")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStorageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}