package com.example.clockapp

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView

/*
 * MainActivity.kt
 * ---------------
 * This is the ENTRY POINT of the app. Every Android app starts here.
 * It acts like a "container" that holds different screens (fragments).
 *
 * KOTLIN vs JAVA differences you'll notice here:
 * - No semicolons at end of lines
 * - Variables declared with  "val" (immutable) or "var" (mutable)
 * - "lateinit var" = a var that we promise to set before using it
 * - findViewById<Type> uses Kotlin generics instead of casting
 * - Lambdas are shorter: { } instead of new OnClickListener() { ... }
 * - "when" replaces Java's "switch"
 */
class MainActivity : Activity() {

    // lateinit = we don't set these in the constructor,
    // but we GUARANTEE they'll be set in onCreate() before any use
    private lateinit var navClock: TextView
    private lateinit var navAlarm: TextView
    private lateinit var navTimer: TextView
    private lateinit var navStopwatch: TextView
    private lateinit var fragmentContainer: FrameLayout

    // Track which screen is currently showing
    private var currentScreen = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // In Kotlin, findViewById needs the type in angle brackets
        fragmentContainer = findViewById(R.id.fragment_container)
        navClock          = findViewById(R.id.nav_clock)
        navAlarm          = findViewById(R.id.nav_alarm)
        navTimer          = findViewById(R.id.nav_timer)
        navStopwatch      = findViewById(R.id.nav_stopwatch)

        /*
         * Kotlin lambda shorthand for click listeners:
         * Instead of:  setOnClickListener(new View.OnClickListener() { public void onClick(View v) { ... } })
         * Kotlin uses:  setOnClickListener { showScreen("clock") }
         *
         * The { } IS the anonymous function. "it" would be the View parameter if needed.
         */
        navClock.setOnClickListener     { showScreen("clock") }
        navAlarm.setOnClickListener     { showScreen("alarm") }
        navTimer.setOnClickListener     { showScreen("timer") }
        navStopwatch.setOnClickListener { showScreen("stopwatch") }

        // Show Clock screen by default
        showScreen("clock")
    }

    /*
     * showScreen() - swaps which Fragment is visible.
     *
     * Kotlin "when" = Java's "switch" but more powerful:
     * - No "break" needed (no fall-through)
     * - Can match strings, ranges, types, etc.
     */
    private fun showScreen(screenName: String) {
        if (screenName == currentScreen) return
        currentScreen = screenName

        resetNavColors()

        val transaction = fragmentManager.beginTransaction()

        // "when" in Kotlin is like a smarter switch statement
        val fragment: Fragment = when (screenName) {
            "clock"     -> { navClock.setTextColor(0xFF6200EE.toInt());     ClockFragment() }
            "alarm"     -> { navAlarm.setTextColor(0xFF6200EE.toInt());     AlarmFragment() }
            "timer"     -> { navTimer.setTextColor(0xFF6200EE.toInt());     TimerFragment() }
            "stopwatch" -> { navStopwatch.setTextColor(0xFF6200EE.toInt()); StopwatchFragment() }
            else        -> ClockFragment() // default fallback (Kotlin "when" needs an else)
        }

        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    private fun resetNavColors() {
        // In Kotlin, we can call the same method on multiple objects using "apply"
        // but here simple assignment is clearer for a beginner:
        val grey = 0xFF888888.toInt()
        navClock.setTextColor(grey)
        navAlarm.setTextColor(grey)
        navTimer.setTextColor(grey)
        navStopwatch.setTextColor(grey)
    }
}
