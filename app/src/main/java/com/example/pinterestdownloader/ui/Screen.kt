package com.example.pinterestdownloader.ui

sealed class Screen(val route: String) {
    object Input : Screen("input")
    object Downloader : Screen("downloader")
    object Settings : Screen("settings")
    object DownloadList : Screen("download_list")
}
