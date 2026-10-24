package com.example.kancentration.data.model

data class DlcPackItem(
    val id: String,
    val title: String,
    val description: String,
    val priceFormatted: String,
    val storagePath: String,
    val quadCount: Int,
    val isInstalled: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f
)

sealed interface DlcDownloadState {
    object Idle : DlcDownloadState
    data class Downloading(val packId: String, val progress: Float) : DlcDownloadState
    data class Success(val packId: String) : DlcDownloadState
    data class Error(val packId: String, val message: String) : DlcDownloadState
}
