package com.example.kancentration.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kancentration.data.firebase.DlcManager
import com.example.kancentration.data.repository.KanjiRepository
import com.example.kancentration.data.repository.SettingsRepository
import com.example.kancentration.domain.GameEngine
import com.example.kancentration.domain.SoundPlayer
import com.example.kancentration.domain.TimerManager

/**
 * Custom ViewModelProvider.Factory that injects pure decoupled interfaces
 * (KanjiRepository, SettingsRepository, TimerManager, DlcManager, SoundPlayer)
 * into GameViewModel.
 */
class GameViewModelFactory(
    private val repository: KanjiRepository,
    private val settingsRepository: SettingsRepository,
    private val timerManager: TimerManager,
    private val dlcManager: DlcManager,
    private val soundManager: SoundPlayer,
    private val gameEngine: GameEngine = GameEngine()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(
                repository = repository,
                settingsRepository = settingsRepository,
                dlcManager = dlcManager,
                soundManager = soundManager,
                timerManager = timerManager,
                gameEngine = gameEngine
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
