package com.example.kancentration.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.model.GameSettings
import com.example.kancentration.data.model.GameType

interface SettingsRepository {
    fun loadSettings(): GameSettings
    fun saveSettings(settings: GameSettings)

    companion object {
        operator fun invoke(prefs: SharedPreferences): SettingsRepository =
            SharedPreferencesSettingsRepository(prefs)

        operator fun invoke(context: Context): SettingsRepository =
            SharedPreferencesSettingsRepository(context)
    }
}

class SharedPreferencesSettingsRepository(
    private val prefs: SharedPreferences
) : SettingsRepository {

    constructor(context: Context) : this(
        context.getSharedPreferences("kancentration_settings", Context.MODE_PRIVATE)
    )

    override fun loadSettings(): GameSettings {
        val dimStr = prefs.getString(KEY_DIMENSION, BoardDimension.FOUR_BY_FOUR.name)
        val dimension = try {
            BoardDimension.valueOf(dimStr ?: BoardDimension.FOUR_BY_FOUR.name)
        } catch (_: Exception) {
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
        val twinTrapHints = prefs.getBoolean(KEY_TWIN_TRAP_HINTS, true)
        val showRomaji = prefs.getBoolean(KEY_SHOW_ROMAJI, false)
        val useSerifFont = prefs.getBoolean(KEY_USE_SERIF_FONT, true)
        val soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        val tileColor = prefs.getLong(KEY_TILE_COLOR, 0xFFFFFFFFL)
        val kanjiColor = prefs.getLong(KEY_KANJI_COLOR, 0xFFD4AF37L)
        val mismatchDelayMs = prefs.getLong(KEY_MISMATCH_DELAY_MS, 1150L)
        val showStrokeCount = prefs.getBoolean(KEY_SHOW_STROKE_COUNT, true)
        val selectedPackId = prefs.getString(KEY_SELECTED_PACK, "base_pack") ?: "base_pack"

        return GameSettings(
            dimension = dimension,
            gameType = gameType,
            bonusTurnOnMatch = bonusTurn,
            showTwinTrapHints = twinTrapHints,
            showRomaji = showRomaji,
            useSerifFont = useSerifFont,
            soundEnabled = soundEnabled,
            tileColor = tileColor,
            kanjiColor = kanjiColor,
            mismatchDelayMs = mismatchDelayMs,
            showStrokeCount = showStrokeCount,
            selectedPackId = selectedPackId
        )
    }

    override fun saveSettings(settings: GameSettings) {
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
            .putBoolean(KEY_TWIN_TRAP_HINTS, settings.showTwinTrapHints)
            .putBoolean(KEY_SHOW_ROMAJI, settings.showRomaji)
            .putBoolean(KEY_USE_SERIF_FONT, settings.useSerifFont)
            .putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
            .putLong(KEY_TILE_COLOR, settings.tileColor)
            .putLong(KEY_KANJI_COLOR, settings.kanjiColor)
            .putLong(KEY_MISMATCH_DELAY_MS, settings.mismatchDelayMs)
            .putBoolean(KEY_SHOW_STROKE_COUNT, settings.showStrokeCount)
            .putString(KEY_SELECTED_PACK, settings.selectedPackId)
            .apply()
    }

    companion object {
        private const val KEY_DIMENSION = "pref_dimension"
        private const val KEY_GAME_TYPE = "pref_game_type"
        private const val KEY_TIME_ATTACK_MINUTES = "pref_time_attack_minutes"
        private const val KEY_BONUS_TURN = "pref_bonus_turn"
        private const val KEY_TWIN_TRAP_HINTS = "pref_twin_trap_hints"
        private const val KEY_SHOW_ROMAJI = "pref_show_romaji"
        private const val KEY_USE_SERIF_FONT = "pref_use_serif_font"
        private const val KEY_SOUND_ENABLED = "pref_sound_enabled"
        private const val KEY_TILE_COLOR = "pref_tile_color"
        private const val KEY_KANJI_COLOR = "pref_kanji_color"
        private const val KEY_MISMATCH_DELAY_MS = "pref_mismatch_delay_ms"
        private const val KEY_SHOW_STROKE_COUNT = "pref_show_stroke_count"
        private const val KEY_SELECTED_PACK = "pref_selected_pack"
    }
}

class InMemorySettingsRepository(
    initialSettings: GameSettings = GameSettings()
) : SettingsRepository {
    private var currentSettings: GameSettings = initialSettings

    override fun loadSettings(): GameSettings = currentSettings

    override fun saveSettings(settings: GameSettings) {
        currentSettings = settings
    }
}
