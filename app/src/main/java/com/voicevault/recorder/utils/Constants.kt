package com.voicevault.recorder.utils

// all the constant values used across the app - easier to change in one place
object Constants {

    // database
    const val DATABASE_NAME = "voicevault_db"
    const val DATABASE_VERSION = 1

    // recording quality settings - bitrate and sample rate combos
    enum class RecordingQuality(val bitrate: Int, val sampleRate: Int, val displayName: String) {
        LOW(64000, 44100, "Low 64kbps, 44.1kHz"),
        MEDIUM(128000, 44100, "Medium 128kbps, 44.1kHz"),
        HIGH(256000, 48000, "High 256kbps, 48kHz")
    }

    // audio format we're using
    const val AUDIO_FORMAT = "m4a"
    const val AUDIO_MIME_TYPE = "audio/mp4a-latm"

    // where recordings get saved
    const val RECORDINGS_FOLDER = "VoiceVault/Recordings"
    const val TEMP_RECORDINGS_FOLDER = "VoiceVault/Temp"

    // vosk model paths in assets folder
    const val VOSK_MODEL_EN = "models/vosk-model-small-en-us-0.15"
    const val VOSK_MODEL_HI = "models/vosk-model-small-hi-0.22"

    // llm provider names
    const val PROVIDER_GROQ = "groq"
    const val PROVIDER_GEMINI = "gemini"
    const val PROVIDER_EURON = "euron"
    const val PROVIDER_OPENROUTER = "openrouter"
    const val PROVIDER_MISTRAL = "mistral"
    const val PROVIDER_CUSTOM = "custom"

    // groq config
    const val GROQ_BASE_URL = "https://api.groq.com/openai/v1/"
    val GROQ_MODELS = listOf("llama-3.3-70b-versatile", "mixtral-8x7b-32768")

    // gemini config - uses google sdk directly, no base url
    val GEMINI_MODELS = listOf("gemini-2.5-flash", "gemini-1.5-pro")

    // euron config
    const val EURON_BASE_URL = "https://api.euron.one/api/v1/euri/"
    val EURON_MODELS = listOf("gpt-4.1-nano", "gpt-4o-mini")

    // openrouter config
    const val OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/"
    val OPENROUTER_MODELS = listOf(
        "meta-llama/llama-3.1-8b-instruct:free",
        "mistralai/mistral-7b-instruct:free"
    )

    // mistral config
    const val MISTRAL_BASE_URL = "https://api.mistral.ai/v1/"
    val MISTRAL_MODELS = listOf("mistral-small-latest", "mistral-tiny")

    // waveform visualization settings
    const val WAVEFORM_SAMPLES_PER_SECOND = 10
    const val WAVEFORM_MAX_AMPLITUDE = 32767f // max value for 16-bit audio

    // playback speed options
    val PLAYBACK_SPEEDS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f)

    // preferences keys
    const val PREF_RECORDING_QUALITY = "recording_quality"
    const val PREF_BLOCK_CALLS = "block_calls_while_recording"
    const val PREF_AUTO_PLAY_NEXT = "auto_play_next"
    const val PREF_USE_BLUETOOTH = "use_bluetooth_mic"
    const val PREF_STORAGE_LOCATION = "storage_location"
    const val PREF_RECYCLE_BIN_ENABLED = "recycle_bin_enabled"
    const val PREF_SAVE_SEARCHES = "save_recent_searches"
    const val PREF_THEME = "app_theme"
    const val PREF_AUTO_TRANSCRIBE = "auto_transcribe"
    const val PREF_LLM_PROVIDER = "llm_provider"
    const val PREF_LLM_MODEL = "llm_model"
    const val PREF_API_KEY_PREFIX = "api_key_"

    // storage location options
    const val STORAGE_INTERNAL = "internal"
    const val STORAGE_SD_CARD = "sd_card"

    // recycle bin retention
    const val RECYCLE_BIN_DAYS = 30
}