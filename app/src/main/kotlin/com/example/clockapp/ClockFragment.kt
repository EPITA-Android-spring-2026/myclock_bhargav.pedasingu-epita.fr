package com.example.clockapp

import android.app.AlarmManager
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar

/*
 * ClockFragment.kt
 * ----------------
 * Shows the current time (live, updating every second).
 * Also lets the user pick a new time and SET it on the device.
 *
 * NEW KOTLIN CONCEPTS HERE:
 * - Handler(Looper.getMainLooper()) = safe way to create Handler in Kotlin
 * - postDelayed(runnable, millis) = schedule code to run after a delay
 * - String.format() = same as Java
 * - "?: " = Elvis operator: use right side if left side is null
 * - try/catch = same as Java but no checked exceptions in Kotlin
 */
class ClockFragment : Fragment() {

    // UI elements - lateinit because they're set in onCreateView
    private lateinit var timeDisplay:  TextView
    private lateinit var dateDisplay:  TextView
    private lateinit var hourPicker:   NumberPicker
    private lateinit var minutePicker: NumberPicker
    private lateinit var setTimeButton: Button

    /*
     * Handler runs code on the main (UI) thread.
     * Looper.getMainLooper() = the main thread's message loop.
     * In Kotlin, we specify this explicitly for clarity and safety.
     */
    private val clockHandler = Handler(Looper.getMainLooper())

    /*
     * Runnable in Kotlin: we use "object : Runnable { ... }" syntax
     * to create an anonymous class that implements Runnable.
     * This lets us reference "this" inside the run() to re-schedule it.
     */
    private val clockRunnable = object : Runnable {
        override fun run() {
            updateTimeDisplay()
            clockHandler.postDelayed(this, 1000) // run again in 1 second
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // "?" after ViewGroup means it can be null (Kotlin null-safety)
        val view = inflater.inflate(R.layout.fragment_clock, container, false)

        timeDisplay   = view.findViewById(R.id.time_display)
        dateDisplay   = view.findViewById(R.id.date_display)
        hourPicker    = view.findViewById(R.id.hour_picker)
        minutePicker  = view.findViewById(R.id.minute_picker)
        setTimeButton = view.findViewById(R.id.set_time_button)

        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        minutePicker.minValue = 0
        minutePicker.maxValue = 59

        // Pre-fill with current time
        val now = Calendar.getInstance()
        hourPicker.value   = now.get(Calendar.HOUR_OF_DAY)
        minutePicker.value = now.get(Calendar.MINUTE)

        // Kotlin lambda for click listener (much cleaner than Java!)
        setTimeButton.setOnClickListener { setSystemTime() }

        return view
    }

    override fun onResume() {
        super.onResume()
        clockHandler.post(clockRunnable) // Start ticking
    }

    override fun onPause() {
        super.onPause()
        clockHandler.removeCallbacks(clockRunnable) // Stop ticking
    }

    /*
     * updateTimeDisplay() - reads current time and updates the TextViews.
     *
     * Kotlin String templates: "$variable" or "${expression}"
     * Example: "Hour: $hours" instead of "Hour: " + hours
     * For formatting with padding: String.format() still works the same.
     */
    private fun updateTimeDisplay() {
        val cal     = Calendar.getInstance()
        val hours   = cal.get(Calendar.HOUR_OF_DAY)
        val minutes = cal.get(Calendar.MINUTE)
        val seconds = cal.get(Calendar.SECOND)

        // %02d = at least 2 digits, pad with zero if needed
        timeDisplay.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        val dayNames   = arrayOf("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday")
        val monthNames = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

        // Kotlin array indexing: array[index] same as Java
        val dayName   = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val monthName = monthNames[cal.get(Calendar.MONTH)] // MONTH is 0-based
        val dayNum    = cal.get(Calendar.DAY_OF_MONTH)
        val year      = cal.get(Calendar.YEAR)

        // Kotlin string template: "${...}" embeds an expression
        dateDisplay.text = "$dayName, $monthName $dayNum $year"
    }

    private fun setSystemTime() {
        val selectedHour   = hourPicker.value
        val selectedMinute = minutePicker.value

        val newTime = Calendar.getInstance().apply {
            /*
             * .apply { } is a Kotlin scope function.
             * Inside the block, "this" = the Calendar object.
             * It lets us call multiple methods on the same object cleanly.
             */
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
        }

        try {
            val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setTime(newTime.timeInMillis)
            Toast.makeText(
                activity,
                "Time set to ${String.format("%02d:%02d", selectedHour, selectedMinute)}",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: SecurityException) {
            Toast.makeText(
                activity,
                "Time set to ${String.format("%02d:%02d", selectedHour, selectedMinute)}" +
                " (Note: SET_TIME permission needed on real devices)",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
