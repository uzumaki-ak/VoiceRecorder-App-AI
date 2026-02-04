package com.voicevault.recorder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

// custom waveform visualization component - FIXED: dynamic bars
@Composable
fun WaveformView(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    currentPosition: Long = 0,
    duration: Long = 0,
    bookmarks: List<Long> = emptyList(),
    onSeek: ((Long) -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                if (onSeek != null && duration > 0) {
                    detectTapGestures { offset ->
                        val position = (offset.x / size.width) * duration
                        onSeek(position.toLong())
                    }
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2

        if (amplitudes.isEmpty()) return@Canvas

        // FIXED: calculate bar width and spacing
        val barCount = amplitudes.size.coerceAtMost(100) // limit to 100 bars for performance
        val barWidth = (canvasWidth / barCount) * 0.6f // 60% width, 40% gap
        val spacing = canvasWidth / barCount

        // FIXED: draw waveform bars with proper spacing
        amplitudes.take(barCount).forEachIndexed { index, amplitude ->
            val x = index * spacing + spacing / 2
            val normalizedAmp = amplitude.coerceIn(0f, 1f)
            val barHeight = (normalizedAmp * centerY * 0.8f).coerceAtLeast(4f) // min 4px height

            // draw bar - FIXED: actual vertical bars
            drawLine(
                color = if (duration > 0 && currentPosition > 0) {
                    // color bars before current position differently
                    val barPosition = (index.toFloat() / barCount) * duration
                    if (barPosition <= currentPosition) {
                        color.copy(alpha = 1f)
                    } else {
                        color.copy(alpha = 0.3f)
                    }
                } else {
                    color
                },
                start = Offset(x, centerY - barHeight),
                end = Offset(x, centerY + barHeight),
                strokeWidth = barWidth
            )
        }

        // FIXED: draw current position indicator if playing
        if (duration > 0 && currentPosition > 0) {
            val progressX = (currentPosition.toFloat() / duration.toFloat()) * canvasWidth
            drawLine(
                color = Color.Red,
                start = Offset(progressX, 0f),
                end = Offset(progressX, canvasHeight),
                strokeWidth = 3f
            )
        }

        // FIXED: draw bookmarks
        bookmarks.forEach { bookmarkPosition ->
            if (duration > 0) {
                val bookmarkX = (bookmarkPosition.toFloat() / duration.toFloat()) * canvasWidth
                drawCircle(
                    color = Color.Blue,
                    radius = 8f,
                    center = Offset(bookmarkX, canvasHeight - 16f)
                )
            }
        }
    }
}