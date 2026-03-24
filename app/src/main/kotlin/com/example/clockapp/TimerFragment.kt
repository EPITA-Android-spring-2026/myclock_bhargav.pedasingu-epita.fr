package com.example.clockapp

import android.app.Fragment
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView

/*
 * TimerFragment.kt
 * ----------------
 * Countdown timer: user sets HH:MM:SS, tap Start, counts to 00:00:00.
 *
 * KOTLIN CONCEPTS HERE:
 * - Nullable types: "CountDownTimer?" means it can be null
 *   Use "?.cancel()" (safe call) to only call if not null
 * - "when" expression with no argument = like if/else if chain
 * - Long arithmetic (no casting needed for most operations)
 * - Enum-like state with simple strings (readable and simple)
 */
class TimerFragment : Fragment() {

    private lateinit var hourPicker:   NumberPicker
    private lateinit var minutePicker: NumberPicker
    private lateinit var secondPicker: NumberPicker
    private lateinit var timerDisplay: TextView
    private lateinit var startButton:  Button
    private lateinit var pauseButton:  Button
    private lateinit var resetButton:  Button

    /*
     * "?" after the type = NULLABLE. This variable can hold null.
     * We start it as null because no timer exists until user taps Start.
     * countDownTimer?.cancel() = "call cancel() only if not null" (safe call)
     */
    private var countDownTimer: CountDownTimer? = null

    private var millisRemaining = 0L   // "L" suffix = Long literal
    private var timerState = "idle"    // "idle", "running", or "paused"

    companion object {
        const val CHANNEL_ID = "timer_channel"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)

        hourPicker   = view.findViewById(R.id.timer_hour_picker)
        minutePicker = view.findViewById(R.id.timer_minute_picker)
        secondPicker = view.findViewById(R.id.timer_second_picker)
        timerDisplay = view.findViewById(R.id.timer_display)
        startButton  = view.findViewById(R.id.timer_start_button)
        pauseButton  = view.findViewById(R.id.timer_pause_button)
        resetButton  = view.findViewById(R.id.timer_reset_button)

        hourPicker.minValue   = 0;  hourPicker.maxValue   = 23
        minutePicker.minValue = 0;  minutePicker.maxValue = 59
        secondPicker.minValue = 0;  secondPicker.maxValue = 59
        minutePicker.value = 1  // Default: 1 minute

        startButton.setOnClickListener { startTimer() }
        pauseButton.setOnClickListener { pauseTimer() }
        resetButton.setOnClickListener { resetTimer() }

        updateButtonStates()
        return view
    }

    private fun startTimer() {
        if (timerState == "idle") {
            // Convert picker values to milliseconds
            // Kotlin: toLong() converts Int to Long for the arithmetic
            val hours   = hourPicker.value.toLong()
            val minutes = minutePicker.value.toLong()
            val seconds = secondPicker.value.toLong()

            millisRemaining = (hours * 3600 + minutes * 60 + seconds) * 1000
            if (millisRemaining <= 0) return
        }

        timerState = "running"
        updateButtonStates()

        /*
         * Anonymous object implementing CountDownTimer.
         * "object : CountDownTimer(totalMs, intervalMs)" is Kotlin's way
         * to extend an abstract class on the fly (like Java's anonymous class).
         */
        countDownTimer = object : CountDownTimer(millisRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                millisRemaining = millisUntilFinished
                updateTimerDisplay(millisUntilFinished)
            }

            override fun onFinish() {
                timerDisplay.text = "00:00:00"
                timerState = "idle"
                millisRemaining = 0
                updateButtonStates()
                showTimerFinishedNotification()
            }
        }.start()  // .start() returns the CountDownTimer itself — we chain it here
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()  // "?." = safe call: only calls if not null
        timerState = "paused"
        updateButtonStates()
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        timerState = "idle"
        millisRemaining = 0
        timerDisplay.text = "00:00:00"
        updateButtonStates()
    }

    /*
     * updateTimerDisplay() - converts ms → HH:MM:SS
     * Note: Kotlin integer division "/" truncates like Java
     * "%" is the remainder (modulo) operator
     */
    private fun updateTimerDisplay(millis: Long) {
        val totalSeconds = millis / 1000
        val hours   = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        timerDisplay.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun updateButtonStates() {
        /*
         * Kotlin "when" without an argument works like if/else-if:
         * Each branch is checked in order until one is true.
         */
        startButton.isEnabled = timerState == "idle" || timerState == "paused"
        pauseButton.isEnabled = timerState == "running"
        resetButton.isEnabled = timerState == "running" || timerState == "paused"

        // Change button text: Kotlin ternary-equivalent uses if/else as expression
        startButton.text = if (timerState == "paused") "Resume" else "Start"
    }

    private fun showTimerFinishedNotification() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Timer Notifications", NotificationManager.IMPORTANCE_HIGH
        ).apply { enableVibration(true) }

        val manager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notification = android.app.Notification.Builder(activity, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏱ Timer Finished!")
            .setContentText("Your countdown timer has ended.")
            .setAutoCancel(true)
            .build()

        manager.notify(999, notification)
    }
}
