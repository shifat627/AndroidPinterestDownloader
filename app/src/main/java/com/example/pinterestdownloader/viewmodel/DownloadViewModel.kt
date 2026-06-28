package com.example.pinterestdownloader.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class DownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val fileName: String = "Extracting...",
    val status: String = "Pending",
    val progress: Float = 0f,
    val error: String? = null
)

class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val _downloadLinks = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloadLinks: StateFlow<List<DownloadItem>> = _downloadLinks.asStateFlow()

    private val client = OkHttpClient()
    private val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "download_channel"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun addDownloadLink(url: String) {
        val newItem = DownloadItem(url = url)
        _downloadLinks.value = _downloadLinks.value + newItem
    }

    fun removeDownloadLink(item: DownloadItem) {
        _downloadLinks.value = _downloadLinks.value.filter { it.id != item.id }
    }

    fun clearCompletedDownloads() {
        _downloadLinks.value = _downloadLinks.value.filter { it.status != "Completed" }
    }

    fun startDownloads(videoRegex: String, imageRegex1: String, imageRegex2: String) {
        viewModelScope.launch {
            val pendingItems = _downloadLinks.value.filter { it.status == "Pending" }
            pendingItems.forEach { item ->
                download(item, videoRegex, imageRegex1, imageRegex2)
            }
        }
    }

    private suspend fun download(
        item: DownloadItem,
        videoRegex: String,
        imageRegex1: String,
        imageRegex2: String
    ) {
        updateItem(item.copy(status = "Downloading", fileName = "Fetching content..."))

        try {
            val content = fetchContent(item.url)
            if (content == null) {
                updateItem(item.copy(status = "Failed", error = "Failed to fetch page content"))
                return
            }

            var downloadUrl = extractMatch(content, videoRegex)
            if (downloadUrl == null) {
                val step1Match = extractMatch(content, imageRegex1)
                if (step1Match != null) {
                    downloadUrl = extractMatch(step1Match, imageRegex2)
                }
            }

            if (downloadUrl == null) {
                updateItem(item.copy(status = "Failed", fileName = "No link found", error = "Could not find media link"))
                return
            }

            val extractedFileName = downloadUrl.substringAfterLast("/").substringBefore("?")
            updateItem(item.copy(fileName = extractedFileName))

            performDownload(item.id, downloadUrl, extractedFileName)

        } catch (e: Exception) {
            updateItem(item.copy(status = "Failed", error = e.message))
        }
    }

    private suspend fun fetchContent(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                response.body?.string()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractMatch(content: String, regexStr: String): String? {
        return try {
            val regex = Regex(regexStr)
            val match = regex.find(content)
            match?.groups?.get(1)?.value ?: match?.value
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun performDownload(id: String, url: String, fileName: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        val sharedPreferences = getApplication<Application>().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val directoryUriStr = sharedPreferences.getString("download_directory", "")

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Failed to download file: $response")

                val body = response.body ?: throw Exception("Empty response body")
                val totalBytes = body.contentLength()
                
                val outputStream = if (!directoryUriStr.isNullOrEmpty()) {
                    val directoryUri = Uri.parse(directoryUriStr)
                    val directory = DocumentFile.fromTreeUri(getApplication(), directoryUri)
                    val mimeType = if (fileName.endsWith(".mp4")) "video/mp4" else "image/jpeg"
                    val file = directory?.createFile(mimeType, fileName)
                    getApplication<Application>().contentResolver.openOutputStream(file!!.uri)
                } else {
                    val file = File(getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                    FileOutputStream(file)
                }

                body.byteStream().use { input ->
                    outputStream!!.use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var downloadedBytes = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
                            
                            withContext(Dispatchers.Main) {
                                updateItemById(id, progress = progress)
                                showNotification(id.hashCode(), fileName, (progress * 100).toInt())
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    updateItemById(id, status = "Completed", progress = 1f)
                    notificationManager.cancel(id.hashCode())
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                updateItemById(id, status = "Failed", error = e.message)
                notificationManager.cancel(id.hashCode())
            }
        }
    }

    private fun updateItem(newItem: DownloadItem) {
        _downloadLinks.value = _downloadLinks.value.map {
            if (it.id == newItem.id) newItem else it
        }
    }

    private fun updateItemById(id: String, status: String? = null, progress: Float? = null, error: String? = null) {
        _downloadLinks.value = _downloadLinks.value.map {
            if (it.id == id) {
                it.copy(
                    status = status ?: it.status,
                    progress = progress ?: it.progress,
                    error = error ?: it.error
                )
            } else it
        }
    }

    private fun showNotification(notificationId: Int, fileName: String, progress: Int) {
        val notification = NotificationCompat.Builder(getApplication(), channelId)
            .setContentTitle("Downloading $fileName")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
