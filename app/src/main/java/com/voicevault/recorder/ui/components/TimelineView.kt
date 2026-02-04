package com.voicevault.recorder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

// timeline view showing seconds markers - FIXED
@Composable
fun TimelineView(
    duration: Long,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // convert duration to seconds
        val totalSeconds = (duration / 1000).toInt().coerceAtLeast(10)

        // calculate spacing between markers
        val secondsToShow = 10 // show 10 seconds at a time
        val spacing = canvasWidth / secondsToShow

        // FIXED: draw second markers
        for (i in 0..secondsToShow) {
            val x = i * spacing
            val seconds = (totalSeconds.toFloat() / secondsToShow * i).toInt()

            // draw tick mark
            drawLine(
                color = color.copy(alpha = 0.5f),
                start = Offset(x, canvasHeight * 0.6f),
                end = Offset(x, canvasHeight),
                strokeWidth = 2f
            )

            // FIXED: draw second label
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    this.color = color.toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                val minutes = seconds / 60
                val secs = seconds % 60
                val text = String.format("%02d:%02d", minutes, secs)
                canvas.nativeCanvas.drawText(
                    text,
                    x,
                    canvasHeight * 0.4f,
                    paint
                )
            }
        }
    }
}