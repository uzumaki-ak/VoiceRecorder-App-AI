package com.voicevault.recorder.domain.recorder

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

// handles audio playback with speed control and position tracking
class AudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    // loads audio file for playback
    fun load(file: File, useSpeaker: Boolean = false): Boolean {
        return try {
            release()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(
                            if (useSpeaker) AudioAttributes.CONTENT_TYPE_MUSIC
                            else AudioAttributes.CONTENT_TYPE_SPEECH
                        )
                        .setUsage(
                            if (useSpeaker) AudioAttributes.USAGE_MEDIA
                            else AudioAttributes.USAGE_VOICE_COMMUNICATION
                        )
                        .build()
                )

                setDataSource(file.absolutePath)
                prepare()

                _duration.value = duration.toLong()

                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0L
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // starts or resumes playback
    fun play() {
        mediaPlayer?.apply {
            if (!isPlaying) {
                start()
                _isPlaying.value = true
            }
        }
    }

    // pauses playback
    fun pause() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
                _isPlaying.value = false
            }
        }
    }

    // seeks to specific position in milliseconds
    fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
        _currentPosition.value = position
    }

    // skips forward by milliseconds
    fun skipForward(milliseconds: Long = 1000) {
        mediaPlayer?.let {
            val newPosition = (it.currentPosition + milliseconds).coerceAtMost(it.duration.toLong())
            seekTo(newPosition)
        }
    }

    // skips backward by milliseconds
    fun skipBackward(milliseconds: Long = 1000) {
        mediaPlayer?.let {
            val newPosition = (it.currentPosition - milliseconds).coerceAtLeast(0)
            seekTo(newPosition)
        }
    }

    // sets playback speed
    fun setSpeed(speed: Float) {
        try {
            mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed)!!
            _playbackSpeed.value = speed
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // toggles repeat mode
    fun setRepeat(repeat: Boolean) {
        mediaPlayer?.isLooping = repeat
    }

    // updates current position for ui
    fun updatePosition() {
        mediaPlayer?.let {
            _currentPosition.value = it.currentPosition.toLong()
        }
    }

    // releases media player resources
    fun release() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
            _isPlaying.value = false
            _currentPosition.value = 0L
            _duration.value = 0L
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}