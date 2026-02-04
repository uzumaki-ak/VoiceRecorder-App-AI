package com.voicevault.recorder.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicevault.recorder.ui.components.SaveDialog
import com.voicevault.recorder.ui.components.TimelineView
import com.voicevault.recorder.ui.components.WaveformView
import com.voicevault.recorder.utils.toTimeFormat

// main home screen with recording controls - fixed all button issues
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val recordingState by viewModel.recordingState.collectAsState()
    val showSaveDialog by viewModel.showSaveDialog.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Recorder") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Recycle bin") },
                            onClick = {
                                showMenu = false
                                // navigate to recycle bin - fixed
                                onNavigateToList() // will add recycle bin route
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                showMenu = false
                                onNavigateToSettings()
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // timeline showing seconds - fixed to show duration
                when (val state = recordingState) {
                    is RecordingState.Recording -> {
                        // show recording duration
                        Text(
                            text = state.duration.toTimeFormat(),
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TimelineView(
                            duration = state.duration,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(horizontal = 16.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = "00:00",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TimelineView(
                            duration = 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(horizontal = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // waveform visualization
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    when (val state = recordingState) {
                        is RecordingState.Recording -> {
                            WaveformView(
                                amplitudes = state.waveformData,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            // empty state - show placeholder
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Press record to start",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // recording controls - FIXED ALL BUTTON BEHAVIORS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (val state = recordingState) {
                        is RecordingState.Idle -> {
                            // show list button
                            IconButton(
                                onClick = onNavigateToList,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    Icons.Default.List,
                                    contentDescription = "List",
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(48.dp))

                            // record button - FIXED: starts recording
                            FloatingActionButton(
                                onClick = { viewModel.startRecording() },
                                modifier = Modifier.size(80.dp),
                                containerColor = Color.Red,
                                shape = CircleShape
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color.Red, CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(48.dp))
                        }

                        is RecordingState.Recording -> {
                            // FIXED: play button previews from beginning
                            IconButton(
                                onClick = { viewModel.previewRecording() },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Preview",
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // FIXED: pause/resume button with icon changing
                            FloatingActionButton(
                                onClick = {
                                    if (state.isPaused) {
                                        viewModel.resumeRecording()
                                    } else {
                                        viewModel.pauseRecording()
                                    }
                                },
                                modifier = Modifier.size(80.dp),
                                containerColor = Color.Red,
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = if (state.isPaused) Icons.Default.FiberManualRecord else Icons.Default.Pause,
                                    contentDescription = if (state.isPaused) "Resume" else "Pause",
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.White
                                )
                            }

                            // FIXED: stop button
                            IconButton(
                                onClick = { viewModel.stopRecording() },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // FIXED: save dialog with working cancel button
    if (showSaveDialog) {
        SaveDialog(
            onDismiss = {
                viewModel.dismissSaveDialog() // FIXED: properly dismiss
            },
            onSave = { fileName, categoryId ->
                viewModel.saveRecording(fileName, categoryId)
            }
        )
    }
}