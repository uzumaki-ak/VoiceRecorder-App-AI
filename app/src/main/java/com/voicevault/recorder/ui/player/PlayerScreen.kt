package com.voicevault.recorder.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicevault.recorder.ui.components.WaveformView
import com.voicevault.recorder.utils.toDateFormat
import com.voicevault.recorder.utils.toTimeFormat

// player screen for playing back recordings - ALL FIXED
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    recordingId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    viewModel: PlayerViewModel = viewModel()
) {
    LaunchedEffect(recordingId) {
        viewModel.loadRecording(recordingId)
    }

    val recording by viewModel.currentRecording.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val useSpeaker by viewModel.useSpeaker.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recording?.fileName ?: "Player") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        recording?.let { viewModel.toggleFavorite() }
                    }) {
                        Icon(
                            if (recording?.isFavorite == true) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite"
                        )
                    }
                    IconButton(onClick = { /* edit/trim */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (useSpeaker) "Play through receiver" else "Play through speaker") },
                            onClick = {
                                viewModel.toggleAudioOutput()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = {
                                showMenu = false
                                // show rename dialog
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = {
                                showMenu = false
                                viewModel.shareRecording()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Details") },
                            onClick = {
                                showMenu = false
                                showDetailsDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Chat with AI") },
                            onClick = {
                                showMenu = false
                                recording?.let { onNavigateToChat(it.id) }
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // FIXED: timer display showing current position
            Text(
                text = playbackState.currentPosition.toTimeFormat(),
                style = MaterialTheme.typography.displayMedium
            )

            Text(
                text = playbackState.duration.toTimeFormat(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // FIXED: waveform with seekable timeline and dynamic progress
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                WaveformView(
                    amplitudes = playbackState.waveformData,
                    currentPosition = playbackState.currentPosition,
                    duration = playbackState.duration,
                    bookmarks = playbackState.bookmarks,
                    onSeek = { position -> viewModel.seekTo(position) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // list and bookmark buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LIST")
                }

                TextButton(onClick = { viewModel.addBookmark() }) {
                    Icon(Icons.Default.Bookmark, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BOOKMARK")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // playback controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { viewModel.toggleSkipSilences() }) {
                        Icon(Icons.Default.FastForward, contentDescription = "Skip silences")
                    }
                    Text("Skip silences", style = MaterialTheme.typography.bodySmall)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { viewModel.toggleRepeat() }) {
                        Icon(
                            if (playbackState.isRepeat) Icons.Default.Repeat else Icons.Default.RepeatOn,
                            contentDescription = "Repeat"
                        )
                    }
                    Text("Repeat", style = MaterialTheme.typography.bodySmall)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(onClick = { viewModel.changeSpeed() }) {
                        Text("${playbackState.speed}x")
                    }
                    Text("Speed", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FIXED: main playback controls with dynamic pause/play icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { viewModel.skipBackward() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            Icons.Default.FastRewind,
                            contentDescription = "Back 1s",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text("-1s", style = MaterialTheme.typography.bodySmall)
                }

                // FIXED: Play/Pause button with icon changing
                FloatingActionButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(40.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { viewModel.skipForward() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            Icons.Default.FastForward,
                            contentDescription = "Forward 1s",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text("+1s", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // FIXED: Details dialog
    if (showDetailsDialog && recording != null) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = { Text("Recording Details") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Name: ${recording!!.fileName}")
                    Text("Duration: ${recording!!.duration.toTimeFormat()}")
                    Text("Size: ${recording!!.fileSize / 1024} KB")
                    Text("Bitrate: ${recording!!.bitrate / 1000} kbps")
                    Text("Sample Rate: ${recording!!.sampleRate} Hz")
                    Text("Created: ${recording!!.createdAt.toDateFormat()}")
                    Text("Path: ${recording!!.filePath}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}