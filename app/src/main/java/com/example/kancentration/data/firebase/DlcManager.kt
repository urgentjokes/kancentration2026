package com.example.kancentration.data.firebase

import com.example.kancentration.data.model.DlcDownloadState
import com.example.kancentration.data.model.DlcPackItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface DlcManager {
    val catalogPacks: StateFlow<List<DlcPackItem>>
    val downloadState: StateFlow<DlcDownloadState>
    fun isPackInstalled(packId: String): Boolean
    suspend fun downloadPack(pack: DlcPackItem, onComplete: (Boolean) -> Unit)
}

class FakeDlcManager(
    initialCatalog: List<DlcPackItem> = emptyList()
) : DlcManager {
    private val _catalogPacks = MutableStateFlow(initialCatalog)
    override val catalogPacks: StateFlow<List<DlcPackItem>> = _catalogPacks.asStateFlow()

    private val _downloadState = MutableStateFlow<DlcDownloadState>(DlcDownloadState.Idle)
    override val downloadState: StateFlow<DlcDownloadState> = _downloadState.asStateFlow()

    private val installedPacks = mutableSetOf<String>()

    override fun isPackInstalled(packId: String): Boolean = installedPacks.contains(packId)

    override suspend fun downloadPack(pack: DlcPackItem, onComplete: (Boolean) -> Unit) {
        installedPacks.add(pack.id)
        _downloadState.value = DlcDownloadState.Success(pack.id)
        _catalogPacks.value = _catalogPacks.value.map {
            if (it.id == pack.id) it.copy(isInstalled = true) else it
        }
        onComplete(true)
    }
}
