package com.example.kancentration.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerManager(private val scope: CoroutineScope) {

    private var timerJob: Job? = null

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _remainingMillis = MutableStateFlow(0L)
    val remainingMillis: StateFlow<Long> = _remainingMillis.asStateFlow()

    private val _isTimeUp = MutableStateFlow(false)
    val isTimeUp: StateFlow<Boolean> = _isTimeUp.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    var isCountdownMode: Boolean = false
        private set

    private var onTickWarningCallback: (() -> Unit)? = null
    private var onTimeExpiredCallback: (() -> Unit)? = null

    val isRunning: Boolean
        get() = timerJob != null && timerJob?.isActive == true

    fun prepareStopwatch() {
        stop()
        isCountdownMode = false
        _elapsedSeconds.value = 0
        _isTimeUp.value = false
        _isPaused.value = false
    }

    fun startStopwatch() {
        if (isRunning) return
        _isPaused.value = false
        timerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                _elapsedSeconds.value += 1
            }
        }
    }

    fun prepareCountdown(totalMillis: Long, onTickWarning: () -> Unit = {}, onTimeExpired: () -> Unit = {}) {
        stop()
        isCountdownMode = true
        _remainingMillis.value = totalMillis
        _isTimeUp.value = false
        _isPaused.value = false
        onTickWarningCallback = onTickWarning
        onTimeExpiredCallback = onTimeExpired
    }

    fun startCountdown() {
        if (isRunning) return
        _isPaused.value = false
        timerJob = scope.launch {
            while (isActive && _remainingMillis.value > 0) {
                delay(1000L)
                val newRemaining = (_remainingMillis.value - 1000L).coerceAtLeast(0L)
                _remainingMillis.value = newRemaining
                if (newRemaining in 1000L..10000L) {
                    onTickWarningCallback?.invoke()
                }
                if (newRemaining == 0L) {
                    _isTimeUp.value = true
                    onTimeExpiredCallback?.invoke()
                    break
                }
            }
        }
    }

    fun pause() {
        if (isRunning) {
            timerJob?.cancel()
            timerJob = null
            _isPaused.value = true
        }
    }

    fun resume() {
        if (_isPaused.value && !_isTimeUp.value) {
            if (isCountdownMode) {
                startCountdown()
            } else {
                startStopwatch()
            }
        }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        _isPaused.value = false
    }

    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    fun formatMillis(millis: Long): String {
        val totalSec = (millis / 1000).toInt()
        val m = totalSec / 60
        val s = totalSec % 60
        return "%02d:%02d".format(m, s)
    }
}
