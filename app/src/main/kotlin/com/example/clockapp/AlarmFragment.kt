package com.example.clockapp

import android.app.AlarmManager
import android.app.Fragment
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar

/*
 * AlarmFragment.kt
 * ----------------
 * Add multiple alarms and view them in a scrollable list.
 *
 * NEW KOTLIN CONCEPTS HERE:
 * - data class: a simple class just for holding data (like a struct)
 *   Kotlin auto-generates equals(), hashCode(), toString() for it
 * - mutableListOf<>(): Kotlin's way to create an ArrayList
 * - for (item in list): Kotlin's for-each loop
 * - "$variable" string templates used throughout
 */
class AlarmFragment : Fragment() {

    private lateinit var alarmHourPicker:    NumberPicker
    private lateinit var alarmMinutePicker:  NumberPicker
    private lateinit var addAlarmButton:     Button
    private lateinit var alarmListContainer: LinearLayout

    /*
     * DATA CLASS - Kotlin's cleaner way to hold data.
     * "data class AlarmItem(val id: Int, val hour: Int, val minute: Int)"
     * is equivalent to a Java class with 3 fields + constructor + getters.
     * The "val" means these fields cannot be changed after creation.
     */
    data class AlarmItem(val id: Int, val hour: Int, val minute: Int)

    /*
     * mutableListOf<AlarmItem>() = an ArrayList that can grow/shrink.
     * In Kotlin, we use this instead of new ArrayList<AlarmItem>()
     */
    private val alarmList = mutableListOf<AlarmItem>()

    // Counter for unique alarm IDs
    private var alarmIdCounter = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alarm, container, false)

        alarmHourPicker   = view.findViewById(R.id.alarm_hour_picker)
        alarmMinutePicker = view.findViewById(R.id.alarm_minute_picker)
        addAlarmButton    = view.findViewById(R.id.add_alarm_button)
        alarmListContainer = view.findViewById(R.id.alarm_list_container)

        alarmHourPicker.minValue   = 0
        alarmHourPicker.maxValue   = 23
        alarmMinutePicker.minValue = 0
        alarmMinutePicker.maxValue = 59

        val now = Calendar.getInstance()
        alarmHourPicker.value   = now.get(Calendar.HOUR_OF_DAY)
        alarmMinutePicker.value = (now.get(Calendar.MINUTE) + 1) % 60

        addAlarmButton.setOnClickListener { addAlarm() }

        return view
    }

    private fun addAlarm() {
        val hour   = alarmHourPicker.value
        val minute = alarmMinutePicker.value

        /*
         * .apply { } scope function: sets multiple properties on Calendar
         * without repeating the variable name every time.
         */
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time already passed today, schedule for tomorrow
        if (alarmTime.timeInMillis <= System.currentTimeMillis()) {
            alarmTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val thisAlarmId = alarmIdCounter++  // Use current value, then increment
        val newAlarm    = AlarmItem(thisAlarmId, hour, minute)
        alarmList.add(newAlarm)

        scheduleAlarm(thisAlarmId, alarmTime)
        addAlarmCard(newAlarm)

        Toast.makeText(
            activity,
            "Alarm set for ${String.format("%02d:%02d", hour, minute)}",
            Toast.LENGTH_SHORT
        ).show()
    }

    /*
     * scheduleAlarm() - registers the alarm with AlarmManager.
     *
     * "as AlarmManager" is Kotlin's cast syntax (Java used (AlarmManager) cast)
     * PendingIntent.FLAG_IMMUTABLE is required on Android 12+
     */
    private fun scheduleAlarm(alarmId: Int, alarmTime: Calendar) {
        val intent = Intent(activity, AlarmReceiver::class.java).apply {
            /*
             * ::class.java = Kotlin way to get the Java Class object.
             * AlarmReceiver::class.java is equivalent to Java's AlarmReceiver.class
             */
            putExtra("alarm_id", alarmId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            activity,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            // Kotlin uses "or" for bitwise OR (Java used "|")
        )

        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmTime.timeInMillis,
            pendingIntent
        )
    }

    /*
     * addAlarmCard() - builds a UI row for the alarm in Java code.
     *
     * Notice "val card = LinearLayout(activity).apply { ... }"
     * The .apply block sets up the card's properties — clean Kotlin style.
     */
    private fun addAlarmCard(alarm: AlarmItem) {
        // Create the row card
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(0xFFEEEEFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
        }

        // Time label
        val timeLabel = TextView(activity).apply {
            text      = String.format("%02d:%02d", alarm.hour, alarm.minute)
            textSize  = 22f   // Kotlin uses "f" suffix for Float literals
            setTextColor(0xFF333333.toInt())
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f   // weight = 1, takes all remaining space
            )
        }

        // Delete button
        val deleteBtn = Button(activity).apply {
            text = "Delete"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFE53935.toInt())
            setOnClickListener {
                cancelAlarm(alarm.id)
                alarmList.remove(alarm)
                alarmListContainer.removeView(card)
                Toast.makeText(activity, "Alarm deleted", Toast.LENGTH_SHORT).show()
            }
        }

        card.addView(timeLabel)
        card.addView(deleteBtn)
        alarmListContainer.addView(card)
    }

    private fun cancelAlarm(alarmId: Int) {
        val intent = Intent(activity, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            activity, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
