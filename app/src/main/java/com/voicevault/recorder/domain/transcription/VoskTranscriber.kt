package com.voicevault.recorder.domain.transcription

import android.content.Context
import com.voicevault.recorder.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.File

// handles offline speech recognition using vosk models
class VoskTranscriber(private val context: Context) {

    private var model: Model? = null
    private var recognizer: Recognizer? = null

    // loads the vosk model for specified language
    suspend fun loadModel(language: String = "en"): Boolean = withContext(Dispatchers.IO) {
        try {
            val modelPath = if (language == "hi") {
                Constants.VOSK_MODEL_HI
            } else {
                Constants.VOSK_MODEL_EN
            }

            // check if model exists in assets
            val modelDir = File(context.getExternalFilesDir(null), modelPath)

            if (!modelDir.exists()) {
                // copy model from assets to external storage
                copyModelFromAssets(modelPath, modelDir)
            }

            model = Model(modelDir.absolutePath)
            recognizer = Recognizer(model, 16000.0f)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // transcribes audio file to text
    suspend fun transcribeFile(audioFile: File): String = withContext(Dispatchers.IO) {
        try {
            if (model == null) {
                loadModel()
            }

            val recognizer = Recognizer(model, 16000.0f)
            val inputStream = audioFile.inputStream()
            val buffer = ByteArray(4096)

            val transcription = StringBuilder()

            while (inputStream.read(buffer) >= 0) {
                if (recognizer.acceptWaveForm(buffer, buffer.size)) {
                    val result = recognizer.result
                    // parse json result and extract text
                    transcription.append(parseVoskResult(result))
                }
            }

            // get final result
            val finalResult = recognizer.finalResult
            transcription.append(parseVoskResult(finalResult))

            inputStream.close()
            recognizer.close()

            transcription.toString().trim()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    // parses vosk json result to extract text
    private fun parseVoskResult(result: String): String {
        return try {
            // vosk returns json like {"text": "transcribed text"}
            val textStart = result.indexOf("\"text\" : \"") + 10
            val textEnd = result.indexOf("\"", textStart)
            if (textStart > 10 && textEnd > textStart) {
                result.substring(textStart, textEnd) + " "
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    // copies model from assets to external storage
    private fun copyModelFromAssets(modelPath: String, targetDir: File) {
        targetDir.mkdirs()

        val assetManager = context.assets
        val files = assetManager.list(modelPath) ?: return

        for (file in files) {
            val assetPath = "$modelPath/$file"
            val targetFile = File(targetDir, file)

            if (assetManager.list(assetPath)?.isNotEmpty() == true) {
                // it's a directory, recurse
                copyModelFromAssets(assetPath, targetFile)
            } else {
                // it's a file, copy it
                assetManager.open(assetPath).use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    // releases resources
    fun release() {
        recognizer?.close()
        model?.close()
        recognizer = null
        model = null
    }
}