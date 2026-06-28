package com.example.pinterestdownloader.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import android.content.Intent
import android.net.Uri

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _videoRegex = MutableStateFlow(sharedPreferences.getString("video_regex", "https:\\/\\/[\\d\\w.]+\\/videos\\/([\\d\\w.]+\\/)+[\\d\\w]+.mp4") ?: "")
    val videoRegex: StateFlow<String> = _videoRegex.asStateFlow()

    private val _imageRegexStep1 = MutableStateFlow(sharedPreferences.getString("image_regex_step1", "\"image\":\"(https:\\/\\/[\\d\\w.]+\\/originals\\/([\\d\\w.]+\\/)+[\\d\\w]+.[a-z]{3})\"") ?: "")
    val imageRegexStep1: StateFlow<String> = _imageRegexStep1.asStateFlow()

    private val _imageRegexStep2 = MutableStateFlow(sharedPreferences.getString("image_regex_step2", "(https:\\/\\/[\\d\\w.]+\\/originals\\/([\\d\\w.]+\\/)+[\\d\\w]+.[a-z]{3})") ?: "")
    val imageRegexStep2: StateFlow<String> = _imageRegexStep2.asStateFlow()

    private val _downloadDirectory = MutableStateFlow(sharedPreferences.getString("download_directory", "") ?: "")
    val downloadDirectory: StateFlow<String> = _downloadDirectory.asStateFlow()

    fun updateVideoRegex(value: String) { _videoRegex.value = value }
    fun updateImageRegexStep1(value: String) { _imageRegexStep1.value = value }
    fun updateImageRegexStep2(value: String) { _imageRegexStep2.value = value }

    fun updateDownloadDirectory(uri: Uri) {
        val application = getApplication<Application>()
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        application.contentResolver.takePersistableUriPermission(uri, takeFlags)
        
        _downloadDirectory.value = uri.toString()
    }

    fun saveSettings() {
        sharedPreferences.edit().apply {
            putString("video_regex", _videoRegex.value)
            putString("image_regex_step1", _imageRegexStep1.value)
            putString("image_regex_step2", _imageRegexStep2.value)
            putString("download_directory", _downloadDirectory.value)
            apply()
        }
    }
}
