package com.example.chessapp

import android.os.Handler
import android.os.SystemClock
import android.widget.TextView

class GameTimer(private val timerTextView: TextView) {

    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var handler: Handler = Handler()

    private val tickRunnable: Runnable = object : Runnable {
        override fun run() {
            val currentTime = SystemClock.elapsedRealtime()
            elapsedTime = currentTime - startTime
            updateTimerText(elapsedTime)
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    fun start() {
        startTime = SystemClock.elapsedRealtime() - elapsedTime
        handler.post(tickRunnable)
    }

    fun pause() {
        handler.removeCallbacks(tickRunnable)
    }

    fun reset() {
        elapsedTime = 0
        updateTimerText(elapsedTime)
    }

    private fun updateTimerText(elapsedTime: Long) {
        val seconds = (elapsedTime / 1000).toInt()
        val minutes = seconds / 60
        val hours = minutes / 60

        val formattedTime = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
        timerTextView.text = formattedTime
    }
}
