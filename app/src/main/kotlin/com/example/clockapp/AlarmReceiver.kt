package com.example.clockapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/*
 * AlarmReceiver.kt
 * ----------------
 * A BroadcastReceiver that "wakes up" when our alarm fires.
 * Registered in AndroidManifest.xml so Android knows it exists.
 *
 * KOTLIN CONCEPTS HERE:
 * - companion object: holds constants/static members (like Java's "static")
 *   In Kotlin, classes don't have static members — use companion object instead.
 * - "as NotificationManager": Kotlin cast syntax
 * - String interpolation: "Alarm ID: $alarmId"
 */
class AlarmReceiver : BroadcastReceiver() {

    /*
     * companion object = the "static" section of the class.
     * CHANNEL_ID is a constant: "const val" = compile-time constant (like Java's static final)
     */
    companion object {
        const val CHANNEL_ID = "clock_app_alarm_channel"
    }

    /*
     * onReceive() is called by Android when the broadcast arrives.
     * "context: Context" and "intent: Intent" = Kotlin parameter syntax (name: Type)
     */
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", 0)

        createNotificationChannel(context)

        // Intent to open the app when notification is tapped
        val openAppIntent   = Intent(context, MainActivity::class.java)
        val openAppPending  = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        /*
         * Notification.Builder in Kotlin.
         * Note the method chaining — same as Java but reads cleanly.
         * We use android.R.drawable.ic_lock_idle_alarm (built-in Android icon).
         */
        val notification = android.app.Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ Alarm!")
            .setContentText("Your alarm is ringing! (ID: $alarmId)")
            .setAutoCancel(true)
            .setContentIntent(openAppPending)
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .build()  // .build() creates the final Notification object

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(alarmId, notification)
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarm Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for clock app alarms"
            enableVibration(true)
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
