package com.voicevault.recorder.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.voicevault.recorder.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// extension property to get datastore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

// handles all app preferences using datastore instead of sharedpreferences
class AppPreferences(private val context: Context) {

    // preference keys
    private object PreferenceKeys {
        val RECORDING_QUALITY = stringPreferencesKey(Constants.PREF_RECORDING_QUALITY)
        val BLOCK_CALLS = booleanPreferencesKey(Constants.PREF_BLOCK_CALLS)
        val AUTO_PLAY_NEXT = booleanPreferencesKey(Constants.PREF_AUTO_PLAY_NEXT)
        val USE_BLUETOOTH = booleanPreferencesKey(Constants.PREF_USE_BLUETOOTH)
        val STORAGE_LOCATION = stringPreferencesKey(Constants.PREF_STORAGE_LOCATION)
        val RECYCLE_BIN_ENABLED = booleanPreferencesKey(Constants.PREF_RECYCLE_BIN_ENABLED)
        val SAVE_SEARCHES = booleanPreferencesKey(Constants.PREF_SAVE_SEARCHES)
        val THEME = stringPreferencesKey(Constants.PREF_THEME)
        val AUTO_TRANSCRIBE = booleanPreferencesKey(Constants.PREF_AUTO_TRANSCRIBE)
        val LLM_PROVIDER = stringPreferencesKey(Constants.PREF_LLM_PROVIDER)
        val LLM_MODEL = stringPreferencesKey(Constants.PREF_LLM_MODEL)
    }

    // recording quality
    val recordingQuality: Flow<Constants.RecordingQuality> = context.dataStore.data.map { preferences ->
        val qualityName = preferences[PreferenceKeys.RECORDING_QUALITY] ?: Constants.RecordingQuality.MEDIUM.name
        Constants.RecordingQuality.valueOf(qualityName)
    }

    suspend fun setRecordingQuality(quality: Constants.RecordingQuality) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.RECORDING_QUALITY] = quality.name
        }
    }

    // block calls while recording
    val blockCalls: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.BLOCK_CALLS] ?: false
    }

    suspend fun setBlockCalls(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.BLOCK_CALLS] = enabled
        }
    }

    // auto play next recording
    val autoPlayNext: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.AUTO_PLAY_NEXT] ?: true
    }

    suspend fun setAutoPlayNext(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.AUTO_PLAY_NEXT] = enabled
        }
    }

    // use bluetooth mic
    val useBluetooth: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USE_BLUETOOTH] ?: false
    }

    suspend fun setUseBluetooth(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.USE_BLUETOOTH] = enabled
        }
    }

    // storage location
    val storageLocation: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.STORAGE_LOCATION] ?: Constants.STORAGE_INTERNAL
    }

    suspend fun setStorageLocation(location: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.STORAGE_LOCATION] = location
        }
    }

    // recycle bin enabled
    val recycleBinEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.RECYCLE_BIN_ENABLED] ?: true
    }

    suspend fun setRecycleBinEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.RECYCLE_BIN_ENABLED] = enabled
        }
    }

    // save searches
    val saveSearches: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.SAVE_SEARCHES] ?: true
    }

    suspend fun setSaveSearches(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SAVE_SEARCHES] = enabled
        }
    }

    // auto transcribe
    val autoTranscribe: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.AUTO_TRANSCRIBE] ?: false
    }

    suspend fun setAutoTranscribe(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.AUTO_TRANSCRIBE] = enabled
        }
    }

    // llm provider
    val llmProvider: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.LLM_PROVIDER] ?: Constants.PROVIDER_GROQ
    }

    suspend fun setLlmProvider(provider: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.LLM_PROVIDER] = provider
        }
    }

    // llm model
    val llmModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.LLM_MODEL] ?: Constants.GROQ_MODELS[0]
    }

    suspend fun setLlmModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.LLM_MODEL] = model
        }
    }

    // api key storage - each provider gets its own key
    fun getApiKey(provider: String): Flow<String> = context.dataStore.data.map { preferences ->
        val key = stringPreferencesKey("${Constants.PREF_API_KEY_PREFIX}$provider")
        preferences[key] ?: ""
    }

    suspend fun setApiKey(provider: String, apiKey: String) {
        context.dataStore.edit { preferences ->
            val key = stringPreferencesKey("${Constants.PREF_API_KEY_PREFIX}$provider")
            preferences[key] = apiKey
        }
    }
}