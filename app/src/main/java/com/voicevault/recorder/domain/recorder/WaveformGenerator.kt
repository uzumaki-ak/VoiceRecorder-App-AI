package com.voicevault.recorder.domain.recorder

import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.abs

// generates waveform data from audio files for visualization
class WaveformGenerator {

    // extracts amplitude samples from audio file
    suspend fun generateWaveform(file: File, samplesPerSecond: Int = 10): List<Float> = withContext(Dispatchers.IO) {
        val samples = mutableListOf<Float>()

        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(file.absolutePath)

            // find audio track
            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex == -1) {
                return@withContext emptyList()
            }

            extractor.selectTrack(audioTrackIndex)

            val format = extractor.getTrackFormat(audioTrackIndex)
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

            // calculate how many samples to skip between each data point
            val samplesPerDataPoint = sampleRate / samplesPerSecond

            val buffer = ByteBuffer.allocate(256 * 1024)
            var sampleCount = 0
            var maxAmplitude = 0f

            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                // process buffer to get amplitude
                buffer.rewind()
                val shortBuffer = buffer.asShortBuffer()

                while (shortBuffer.hasRemaining() && sampleCount < samplesPerDataPoint) {
                    val sample = shortBuffer.get().toFloat()
                    maxAmplitude = maxOf(maxAmplitude, abs(sample))
                    sampleCount++
                }

                if (sampleCount >= samplesPerDataPoint) {
                    samples.add(maxAmplitude)
                    maxAmplitude = 0f
                    sampleCount = 0
                }

                extractor.advance()
                buffer.clear()
            }

            // add last sample if any
            if (maxAmplitude > 0) {
                samples.add(maxAmplitude)
            }

            extractor.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext samples
    }

    // generates waveform from live recording amplitude values
    fun generateLiveWaveform(amplitudes: List<Int>): List<Float> {
        return amplitudes.map { it.toFloat() / 32767f } // normalize to 0-1 range
    }
}