package com.example.kancentration.domain

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Lightweight platform sound player utilizing SoundPool with synthetic Japanese
 * pentatonic & Karuta acoustic samples, plus ToneGenerator fallback for low-resource environments.
 */
class SoundManager(private val context: Context) : SoundPlayer {

    private var soundPool: SoundPool? = null
    private var toneGenerator: ToneGenerator? = null
    private var flipSoundId = 0
    private var matchSoundId = 0
    private var mismatchSoundId = 0
    private var twinTrapSoundId = 0
    private var victorySoundId = 0
    private var tickSoundId = 0
    private var isLoaded = false
    override var isMuted = false

    init {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    isLoaded = true
                }
            }

            loadSyntheticSounds()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to ToneGenerator if SoundPool fails
            initToneGeneratorFallback()
        }
    }

    private fun initToneGeneratorFallback() {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (_: Exception) {}
    }

    private fun loadSyntheticSounds() {
        val cacheDir = context.cacheDir

        // 1. Subtle crisp card flip whoosh
        val flipFile = generateWav(cacheDir, "flip.wav", 0.08f) { t, dur ->
            val env = exp(-t * 40.0)
            (sin(2 * PI * (300 + 400 * t / dur) * t) * env).toFloat()
        }

        // 2. Harmonious Japanese pentatonic koto chime (D5 587Hz + A5 880Hz)
        val matchFile = generateWav(cacheDir, "match.wav", 0.45f) { t, _ ->
            val env = exp(-t * 6.0)
            val note1 = sin(2 * PI * 587.33 * t)
            val note2 = sin(2 * PI * 880.0 * t)
            ((note1 * 0.6 + note2 * 0.4) * env).toFloat()
        }

        // 3. Wooden block clack (single hyoshigi strike)
        val mismatchFile = generateWav(cacheDir, "mismatch.wav", 0.12f) { t, _ ->
            val env = exp(-t * 35.0)
            (sin(2 * PI * 220.0 * t) * env).toFloat()
        }

        // 4. Twin-Trap near-miss anomaly alert (double hyoshigi strike with alert pitch)
        val twinTrapFile = generateWav(cacheDir, "twintrap.wav", 0.28f) { t, _ ->
            val strike1 = if (t < 0.12f) sin(2 * PI * 480.0 * t) * exp(-t * 30.0) else 0.0
            val strike2 = if (t >= 0.12f) sin(2 * PI * 360.0 * (t - 0.12f)) * exp(-(t - 0.12f) * 25.0) else 0.0
            ((strike1 + strike2) * 0.9).toFloat()
        }

        // 5. Ascending celebratory victory fanfare
        val victoryFile = generateWav(cacheDir, "victory.wav", 0.8f) { t, dur ->
            val freq = 440.0 + (t / dur) * 440.0
            val env = (1.0 - t / dur) * exp(-t * 2.0)
            (sin(2 * PI * freq * t) * env).toFloat()
        }

        // 6. Soft timer tick
        val tickFile = generateWav(cacheDir, "tick.wav", 0.04f) { t, _ ->
            val env = exp(-t * 80.0)
            (sin(2 * PI * 900.0 * t) * env).toFloat()
        }

        soundPool?.let { pool ->
            flipSoundId = pool.load(flipFile.absolutePath, 1)
            matchSoundId = pool.load(matchFile.absolutePath, 1)
            mismatchSoundId = pool.load(mismatchFile.absolutePath, 1)
            twinTrapSoundId = pool.load(twinTrapFile.absolutePath, 1)
            victorySoundId = pool.load(victoryFile.absolutePath, 1)
            tickSoundId = pool.load(tickFile.absolutePath, 1)
        }
    }

    override fun playFlip() {
        if (!playSound(flipSoundId, 0.7f)) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        }
    }

    override fun playMatch() {
        if (!playSound(matchSoundId, 0.9f)) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
        }
    }

    override fun playMismatch() {
        if (!playSound(mismatchSoundId, 0.6f)) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 100)
        }
    }

    override fun playTwinTrap() {
        if (!playSound(twinTrapSoundId, 0.85f)) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
        }
    }

    override fun playVictory() {
        if (!playSound(victorySoundId, 1.0f)) {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 400)
        }
    }

    override fun playTick() {
        if (!playSound(tickSoundId, 0.5f)) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 30)
        }
    }

    private fun playSound(soundId: Int, volume: Float): Boolean {
        if (isMuted || soundId == 0) return true
        val streamId = soundPool?.play(soundId, volume, volume, 1, 0, 1.0f) ?: 0
        return streamId != 0
    }

    override fun release() {
        soundPool?.release()
        soundPool = null
        toneGenerator?.release()
        toneGenerator = null
    }

    private fun generateWav(dir: File, fileName: String, durationSec: Float, sampleGenerator: (Float, Float) -> Float): File {
        val file = File(dir, fileName)
        val sampleRate = 22050
        val numSamples = (durationSec * sampleRate).toInt()
        val pcmData = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val sample = sampleGenerator(t, durationSec).coerceIn(-1.0f, 1.0f)
            val shortVal = (sample * 32767).toInt().toShort()
            pcmData[i * 2] = (shortVal.toInt() and 0x00FF).toByte()
            pcmData[i * 2 + 1] = ((shortVal.toInt() shr 8) and 0x00FF).toByte()
        }

        FileOutputStream(file).use { out ->
            val totalDataLen = pcmData.size + 36
            val byteRate = sampleRate * 2

            // RIFF header
            out.write("RIFF".toByteArray())
            out.write(intToByteArray(totalDataLen))
            out.write("WAVE".toByteArray())
            // fmt chunk
            out.write("fmt ".toByteArray())
            out.write(intToByteArray(16)) // Subchunk1Size (16 for PCM)
            out.write(shortToByteArray(1)) // AudioFormat (1 for PCM)
            out.write(shortToByteArray(1)) // NumChannels (1 mono)
            out.write(intToByteArray(sampleRate))
            out.write(intToByteArray(byteRate))
            out.write(shortToByteArray(2)) // BlockAlign
            out.write(shortToByteArray(16)) // BitsPerSample
            // data chunk
            out.write("data".toByteArray())
            out.write(intToByteArray(pcmData.size))
            out.write(pcmData)
        }
        return file
    }

    private fun intToByteArray(v: Int): ByteArray = byteArrayOf(
        (v and 0xFF).toByte(),
        ((v shr 8) and 0xFF).toByte(),
        ((v shr 16) and 0xFF).toByte(),
        ((v shr 24) and 0xFF).toByte()
    )

    private fun shortToByteArray(v: Short): ByteArray = byteArrayOf(
        (v.toInt() and 0xFF).toByte(),
        ((v.toInt() shr 8) and 0xFF).toByte()
    )
}
