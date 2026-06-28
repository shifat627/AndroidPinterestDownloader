package com.example.pinterestdownloader.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pinterestdownloader.viewmodel.DownloadViewModel
import com.example.pinterestdownloader.viewmodel.SettingsViewModel

@Composable
fun DownloaderScreen(
    downloadViewModel: DownloadViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToInput: () -> Unit = {}
) {
    val downloads by downloadViewModel.downloadLinks.collectAsState()
    
    val videoRegex by settingsViewModel.videoRegex.collectAsState()
    val imageRegex1 by settingsViewModel.imageRegexStep1.collectAsState()
    val imageRegex2 by settingsViewModel.imageRegexStep2.collectAsState()

    LaunchedEffect(Unit) {
        downloadViewModel.startDownloads(videoRegex, imageRegex1, imageRegex2)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Progress",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = { downloadViewModel.clearCompletedDownloads() }) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = "Clear Completed",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onNavigateToInput) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add More",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(downloads, key = { it.id }) { item ->
                ModernDownloadRow(item)
            }
        }
    }
}

@Composable
fun ModernDownloadRow(item: com.example.pinterestdownloader.viewmodel.DownloadItem) {
    val animatedProgress by animateFloatAsState(targetValue = item.progress, label = "progress")
    val statusColor by animateColorAsState(
        targetValue = when (item.status) {
            "Completed" -> Color(0xFF4CAF50)
            "Failed" -> MaterialTheme.colorScheme.error
            "Downloading" -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        }, label = "status_color"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusIcon(status = item.status, color = statusColor)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fileName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = item.url,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            AnimatedVisibility(visible = item.error != null) {
                item.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            if (item.status == "Downloading" && item.error == null) {
                Text(
                    text = "Downloading...",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StatusIcon(status: String, color: Color) {
    val icon: ImageVector = when (status) {
        "Completed" -> Icons.Default.CheckCircle
        "Failed" -> Icons.Default.Error
        "Downloading" -> Icons.Default.Sync
        else -> Icons.Default.Pending
    }
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}
