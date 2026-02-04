package com.voicevault.recorder.ui.api

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicevault.recorder.data.preferences.AppPreferences
import com.voicevault.recorder.domain.llm.LLMManager
import com.voicevault.recorder.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// viewmodel for api settings screen - manages llm provider configuration
class ApiSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = AppPreferences(application)
    private val llmManager = LLMManager(preferences)

    private val _selectedProvider = MutableStateFlow(Constants.PROVIDER_GROQ)
    val selectedProvider: StateFlow<String> = _selectedProvider.asStateFlow()

    private val _selectedModel = MutableStateFlow("")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferences.llmProvider.collect { provider ->
                _selectedProvider.value = provider
                updateAvailableModels(provider)

                preferences.getApiKey(provider).collect { key ->
                    _apiKey.value = key
                }
            }
        }

        viewModelScope.launch {
            preferences.llmModel.collect { model ->
                _selectedModel.value = model
            }
        }
    }

    fun setProvider(provider: String) {
        viewModelScope.launch {
            _selectedProvider.value = provider
            preferences.setLlmProvider(provider)
            updateAvailableModels(provider)

            // load api key for this provider
            preferences.getApiKey(provider).collect { key ->
                _apiKey.value = key
            }
        }
    }

    fun setModel(model: String) {
        viewModelScope.launch {
            _selectedModel.value = model
            preferences.setLlmModel(model)
        }
    }

    fun setApiKey(key: String) {
        _apiKey.value = key
    }

    fun saveApiKey() {
        viewModelScope.launch {
            preferences.setApiKey(_selectedProvider.value, _apiKey.value)
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _isTesting.value = true
            _testResult.value = null

            // save current settings first
            saveApiKey()
            preferences.setLlmProvider(_selectedProvider.value)
            preferences.setLlmModel(_selectedModel.value)

            // test connection
            llmManager.initializeProvider()
            val result = llmManager.testConnection()

            _isTesting.value = false
            _testResult.value = if (result.isSuccess) {
                "Connection successful!"
            } else {
                "Connection failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    private fun updateAvailableModels(provider: String) {
        _availableModels.value = when (provider) {
            Constants.PROVIDER_GROQ -> Constants.GROQ_MODELS
            Constants.PROVIDER_GEMINI -> Constants.GEMINI_MODELS
            Constants.PROVIDER_EURON -> Constants.EURON_MODELS
            Constants.PROVIDER_OPENROUTER -> Constants.OPENROUTER_MODELS
            Constants.PROVIDER_MISTRAL -> Constants.MISTRAL_MODELS
            else -> emptyList()
        }

        // set first model as default if current model not in list
        if (_selectedModel.value.isEmpty() || _selectedModel.value !in _availableModels.value) {
            _selectedModel.value = _availableModels.value.firstOrNull() ?: ""
        }
    }
}