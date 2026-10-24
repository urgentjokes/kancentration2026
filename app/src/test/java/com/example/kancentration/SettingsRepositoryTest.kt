package com.example.kancentration

import android.content.SharedPreferences
import com.example.kancentration.data.model.BoardDimension
import com.example.kancentration.data.model.GameSettings
import com.example.kancentration.data.model.GameType
import com.example.kancentration.data.repository.SettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {

    private class FakeSharedPreferences : SharedPreferences {
        val map = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = map
        override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
        override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun edit(): SharedPreferences.Editor = FakeEditor(map)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }

    private class FakeEditor(private val map: MutableMap<String, Any?>) : SharedPreferences.Editor {
        override fun putString(key: String?, value: String?): SharedPreferences.Editor { map[key ?: ""] = value; return this }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor { map[key ?: ""] = value; return this }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor { map[key ?: ""] = value; return this }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor { map[key ?: ""] = value; return this }
        override fun remove(key: String?): SharedPreferences.Editor { map.remove(key); return this }
        override fun clear(): SharedPreferences.Editor { map.clear(); return this }
        override fun commit(): Boolean = true
        override fun apply() {}
    }

    @Test
    fun testDefaultSettings() {
        val prefs = FakeSharedPreferences()
        val repo = SettingsRepository(prefs)
        val settings = repo.loadSettings()

        assertEquals(BoardDimension.FOUR_BY_FOUR, settings.dimension)
        assertEquals(GameType.Solo, settings.gameType)
        assertTrue(settings.soundEnabled)
        assertTrue(settings.bonusTurnOnMatch)
        assertEquals(0xFFFFFFFFL, settings.tileColor)
        assertEquals(0xFFD4AF37L, settings.kanjiColor)
    }

    @Test
    fun testSaveAndReloadSettings() {
        val prefs = FakeSharedPreferences()
        val repo = SettingsRepository(prefs)

        val customSettings = GameSettings(
            dimension = BoardDimension.EIGHT_BY_EIGHT,
            gameType = GameType.TimeAttack(5),
            bonusTurnOnMatch = false,
            soundEnabled = false,
            tileColor = 0xFF1E293BL,
            kanjiColor = 0xFF4CAF50L
        )

        repo.saveSettings(customSettings)
        val loaded = repo.loadSettings()

        assertEquals(BoardDimension.EIGHT_BY_EIGHT, loaded.dimension)
        assertTrue(loaded.gameType is GameType.TimeAttack)
        assertEquals(5, (loaded.gameType as GameType.TimeAttack).durationMinutes)
        assertFalse(loaded.bonusTurnOnMatch)
        assertFalse(loaded.soundEnabled)
        assertEquals(0xFF1E293BL, loaded.tileColor)
        assertEquals(0xFF4CAF50L, loaded.kanjiColor)
    }
}
