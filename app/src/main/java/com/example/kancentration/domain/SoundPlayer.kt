package com.example.kancentration.domain

interface SoundPlayer {
    var isMuted: Boolean
    fun playFlip()
    fun playMatch()
    fun playMismatch()
    fun playTwinTrap()
    fun playVictory()
    fun playTick()
    fun release()
}

class NoOpSoundPlayer : SoundPlayer {
    override var isMuted: Boolean = false
    override fun playFlip() {}
    override fun playMatch() {}
    override fun playMismatch() {}
    override fun playTwinTrap() {}
    override fun playVictory() {}
    override fun playTick() {}
    override fun release() {}
}
