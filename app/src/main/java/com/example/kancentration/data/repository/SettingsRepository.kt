package com.example.kancentration.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.model.GameSettings
import com.example.kancentration.data.model.GameType

class SettingsRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences("kancentration_settings", Context.MODE_PRIVATE)
    )

    fun loadSettings(): GameSettings {
        val dimStr = prefs.getString(KEY_DIMENSION, BoardDimension.FOUR_BY_FOUR.name)
        val dimension = try {
            BoardDimension.valueOf(dimStr ?: BoardDimension.FOUR_BY_FOUR.name)
        } catch (e: Exception) {
            BoardDimension.FOUR_BY_FOUR
        }

        val gameTypeStr = prefs.getString(KEY_GAME_TYPE, "SOLO")
        val timeAttackMinutes = prefs.getInt(KEY_TIME_ATTACK_MINUTES, 3)
        val gameType: GameType = when (gameTypeStr) {
            "TWO_PLAYER" -> GameType.TwoPlayer
            "TIME_ATTACK" -> GameType.TimeAttack(timeAttackMinutes)
            else -> GameType.Solo
        }

        val bonusTurn = prefs.getBoolean(KEY_BONUS_TURN, true)
        val soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        val tileColor = prefs.getLong(KEY_TILE_COLOR, 0xFFFFFFFFL)
        val kanjiColor = prefs.getLong(KEY_KANJI_COLOR, 0xFFD4AF37L)

        return GameSettings(
            dimension = dimension,
            gameType = gameType,
            bonusTurnOnMatch = bonusTurn,
            soundEnabled = soundEnabled,
            tileColor = tileColor,
            kanjiColor = kanjiColor
        )
    }

    fun saveSettings(settings: GameSettings) {
        val gameTypeStr = when (settings.gameType) {
            is GameType.Solo -> "SOLO"
            is GameType.TwoPlayer -> "TWO_PLAYER"
            is GameType.TimeAttack -> "TIME_ATTACK"
        }
        val timeAttackMinutes = if (settings.gameType is GameType.TimeAttack) {
            settings.gameType.durationMinutes
        } else {
            3
        }

        prefs.edit()
            .putString(KEY_DIMENSION, settings.dimension.name)
            .putString(KEY_GAME_TYPE, gameTypeStr)
            .putInt(KEY_TIME_ATTACK_MINUTES, timeAttackMinutes)
            .putBoolean(KEY_BONUS_TURN, settings.bonusTurnOnMatch)
            .putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
            .putLong(KEY_TILE_COLOR, settings.tileColor)
            .putLong(KEY_KANJI_COLOR, settings.kanjiColor)
            .apply()
    }

    companion object {
        private const val KEY_DIMENSION = "pref_dimension"
        private const val KEY_GAME_TYPE = "pref_game_type"
        private const val KEY_TIME_ATTACK_MINUTES = "pref_time_attack_minutes"
        private const val KEY_BONUS_TURN = "pref_bonus_turn"
        private const val KEY_SOUND_ENABLED = "pref_sound_enabled"
        private const val KEY_TILE_COLOR = "pref_tile_color"
        private const val KEY_KANJI_COLOR = "pref_kanji_color"
    }
}
