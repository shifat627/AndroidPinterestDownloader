package com.example.pinterestdownloader.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pinterestdownloader.ui.screens.*
import com.example.pinterestdownloader.viewmodel.DownloadViewModel
import com.example.pinterestdownloader.viewmodel.SettingsViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    downloadViewModel: DownloadViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    initialUrl: String? = null,
    onUrlHandled: () -> Unit = {}
) {
    LaunchedEffect(initialUrl) {
        if (initialUrl != null) {
            downloadViewModel.addDownloadLink(initialUrl)
            onUrlHandled()
//            navController.navigate(Screen.Downloader.route) {
//                popUpTo(navController.graph.startDestinationId)
//                launchSingleTop = true
//            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Input.route,
        modifier = modifier
    ) {
        composable(Screen.Input.route) {
            InputScreen(
                viewModel = downloadViewModel,
                onNavigateToDownloader = {
                    navController.navigate(Screen.Downloader.route)
                }
            )
        }
        composable(Screen.Downloader.route) {
            DownloaderScreen(
                downloadViewModel = downloadViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToInput = {
                    navController.navigate(Screen.Input.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }

                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = settingsViewModel)
        }
        composable(Screen.DownloadList.route) {
            DownloadListScreen(settingsViewModel = settingsViewModel)
        }
    }
}
