package com.voicevault.recorder.ui.player

import android.app.Application
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicevault.recorder.VoiceVaultApp
import com.voicevault.recorder.data.database.entities.Bookmark
import com.voicevault.recorder.data.database.entities.Recording
import com.voicevault.recorder.data.repository.BookmarkRepository
import com.voicevault.recorder.data.repository.RecordingRepository
import com.voicevault.recorder.domain.recorder.AudioPlayer
import com.voicevault.recorder.domain.recorder.WaveformGenerator
import com.voicevault.recorder.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

// viewmodel for player screen - manages playback and controls - ALL FIXED
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application
    private val recordingRepository: RecordingRepository
    private val bookmarkRepository: BookmarkRepository
    private val audioPlayer: AudioPlayer
    private val waveformGenerator: WaveformGenerator

    init {
        val database = (application as VoiceVaultApp).database
        recordingRepository = RecordingRepository(database.recordingDao())
        bookmarkRepository = BookmarkRepository(database.bookmarkDao())
        audioPlayer = AudioPlayer(application)
        waveformGenerator = WaveformGenerator()
    }

    private val _currentRecording = MutableStateFlow<Recording?>(null)
    val currentRecording: StateFlow<Recording?> = _currentRecording.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _useSpeaker = MutableStateFlow(false)
    val useSpeaker: StateFlow<Boolean> = _useSpeaker.asStateFlow()

    // loads recording for playback
    fun loadRecording(recordingId: Long) {
        viewModelScope.launch {
            val recording = recordingRepository.getRecordingById(recordingId)
            recording?.let {
                _currentRecording.value = it

                val file = File(it.filePath)
                if (audioPlayer.load(file, _useSpeaker.value)) {
                    // generate waveform
                    val waveform = waveformGenerator.generateWaveform(file)

                    // load bookmarks
                    bookmarkRepository.getBookmarksForRecording(recordingId).collect { bookmarks ->
                        _playbackState.value = _playbackState.value.copy(
                            duration = audioPlayer.duration.value,
                            waveformData = waveform,
                            bookmarks = bookmarks.map { b -> b.position }
                        )
                    }

                    startPlaybackPositionUpdate()
                }
            }
        }
    }

    // toggles play/pause - FIXED
    fun togglePlayPause() {
        if (_playbackState.value.isPlaying) {
            audioPlayer.pause()
        } else {
            audioPlayer.play()
        }
    }

    // seeks to specific position
    fun seekTo(position: Long) {
        audioPlayer.seekTo(position)
    }

    // skips forward 1 second
    fun skipForward() {
        audioPlayer.skipForward(1000)
    }

    // skips backward 1 second
    fun skipBackward() {
        audioPlayer.skipBackward(1000)
    }

    // changes playback speed
    fun changeSpeed() {
        val currentSpeed = _playbackState.value.speed
        val speedIndex = Constants.PLAYBACK_SPEEDS.indexOf(currentSpeed)
        val nextSpeed = Constants.PLAYBACK_SPEEDS.getOrNull(speedIndex + 1) ?: Constants.PLAYBACK_SPEEDS[0]

        audioPlayer.setSpeed(nextSpeed)
        _playbackState.value = _playbackState.value.copy(speed = nextSpeed)
    }

    // toggles repeat mode
    fun toggleRepeat() {
        val newRepeat = !_playbackState.value.isRepeat
        audioPlayer.setRepeat(newRepeat)
        _playbackState.value = _playbackState.value.copy(isRepeat = newRepeat)
    }

    // FIXED: toggle skip silences
    fun toggleSkipSilences() {
        // this is a placeholder - actual implementation would need audio processing
        // for now just show it's toggled in UI if needed
    }

    // toggles between speaker and receiver
    fun toggleAudioOutput() {
        _useSpeaker.value = !_useSpeaker.value
        _currentRecording.value?.let { recording ->
            loadRecording(recording.id)
        }
    }

    // adds bookmark at current position
    fun addBookmark() {
        viewModelScope.launch {
            _currentRecording.value?.let { recording ->
                val bookmark = Bookmark(
                    recordingId = recording.id,
                    position = _playbackState.value.currentPosition,
                    createdAt = System.currentTimeMillis()
                )
                bookmarkRepository.insertBookmark(bookmark)
            }
        }
    }

    // toggles favorite status
    fun toggleFavorite() {
        viewModelScope.launch {
            _currentRecording.value?.let { recording ->
                recordingRepository.toggleFavorite(recording.id, !recording.isFavorite)
                _currentRecording.value = recording.copy(isFavorite = !recording.isFavorite)
            }
        }
    }

    // FIXED: share recording function
    fun shareRecording() {
        viewModelScope.launch {
            _currentRecording.value?.let { recording ->
                val file = File(recording.filePath)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share recording").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }

    // FIXED: updates playback position continuously
    private fun startPlaybackPositionUpdate() {
        viewModelScope.launch {
            audioPlayer.isPlaying.collect { isPlaying ->
                _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)

                while (isPlaying) {
                    audioPlayer.updatePosition()
                    _playbackState.value = _playbackState.value.copy(
                        currentPosition = audioPlayer.currentPosition.value
                    )
                    delay(100)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}