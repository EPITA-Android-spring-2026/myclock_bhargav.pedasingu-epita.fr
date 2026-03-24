package com.example.clockapp

import android.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/*
 * StopwatchFragment.kt
 * --------------------
 * Start, pause (suspend), reset, and record laps.
 *
 * KOTLIN CONCEPTS HERE:
 * - object : Runnable { } = anonymous class implementing Runnable
 * - String.format() with Long math (same as Java)
 * - "it" in lambdas: the single parameter of a lambda is auto-named "it"
 * - Type inference: Kotlin figures out types so we often don't need to write them
 */
class StopwatchFragment : Fragment() {

    private lateinit var stopwatchDisplay: TextView
    private lateinit var startStopButton:  Button
    private lateinit var resetButton:      Button
    private lateinit var lapButton:        Button
    private lateinit var lapContainer:     LinearLayout

    private var isRunning      = false
    private var startTimeMillis = 0L   // When we last pressed Start
    private var elapsedMillis   = 0L   // Accumulated time from previous runs
    private var lapCount        = 0

    /*
     * Handler(Looper.getMainLooper()) = post tasks to the main UI thread.
     * We update every 10ms for centisecond (0.01s) precision display.
     */
    private val stopwatchHandler = Handler(Looper.getMainLooper())

    private val stopwatchRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                val now          = SystemClock.elapsedRealtime()
                val totalElapsed = elapsedMillis + (now - startTimeMillis)
                updateDisplay(totalElapsed)
                stopwatchHandler.postDelayed(this, 10) // 10ms = 100fps
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stopwatch, container, false)

        stopwatchDisplay = view.findViewById(R.id.stopwatch_display)
        startStopButton  = view.findViewById(R.id.start_stop_button)
        resetButton      = view.findViewById(R.id.stopwatch_reset_button)
        lapButton        = view.findViewById(R.id.lap_button)
        lapContainer     = view.findViewById(R.id.lap_container)

        /*
         * Kotlin lambda: the block { } IS the click handler.
         * We don't need to write "View v ->" because we don't use the view parameter.
         */
        startStopButton.setOnClickListener {
            if (isRunning) pauseStopwatch() else startStopwatch()
        }
        resetButton.setOnClickListener { resetStopwatch() }
        lapButton.setOnClickListener   { recordLap() }

        return view
    }

    private fun startStopwatch() {
        isRunning        = true
        startTimeMillis  = SystemClock.elapsedRealtime()
        startStopButton.text = "Pause"
        lapButton.isEnabled  = true
        stopwatchHandler.post(stopwatchRunnable)
    }

    private fun pauseStopwatch() {
        isRunning = false
        // Save accumulated time: add how long THIS run lasted
        elapsedMillis += SystemClock.elapsedRealtime() - startTimeMillis
        startStopButton.text = "Resume"
        stopwatchHandler.removeCallbacks(stopwatchRunnable)
    }

    private fun resetStopwatch() {
        isRunning        = false
        elapsedMillis    = 0L
        startTimeMillis  = 0L
        lapCount         = 0
        stopwatchHandler.removeCallbacks(stopwatchRunnable)
        stopwatchDisplay.text = "00:00.00"
        startStopButton.text  = "Start"
        lapButton.isEnabled   = false
        lapContainer.removeAllViews()
    }

    private fun recordLap() {
        val now          = SystemClock.elapsedRealtime()
        val totalElapsed = elapsedMillis + (now - startTimeMillis)
        lapCount++

        // Build a TextView for this lap dynamically
        val lapView = TextView(activity).apply {
            text      = "Lap $lapCount:   ${formatTime(totalElapsed)}"
            textSize  = 16f
            setTextColor(0xFF333333.toInt())
            setPadding(20, 10, 20, 10)
        }

        lapContainer.addView(lapView, 0) // 0 = insert at TOP (most recent first)
    }

    /*
     * updateDisplay() and formatTime() - convert ms to MM:SS.cc
     *
     * Kotlin: function that returns a value uses "= expression" or "{ return x }"
     * Here we use the block form for clarity.
     */
    private fun updateDisplay(millis: Long) {
        stopwatchDisplay.text = formatTime(millis)
    }

    private fun formatTime(millis: Long): String {
        val minutes      = millis / 60000
        val seconds      = (millis % 60000) / 1000
        val centiseconds = (millis % 1000) / 10
        return String.format("%02d:%02d.%02d", minutes, seconds, centiseconds)
    }

    override fun onPause() {
        super.onPause()
        stopwatchHandler.removeCallbacks(stopwatchRunnable)
    }

    override fun onResume() {
        super.onResume()
        if (isRunning) {
            stopwatchHandler.post(stopwatchRunnable)
        }
    }
}
