package com.kakao.taxi

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat

class ProxyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {

            ACTION_POST -> {
                val notification =
                    intent.getParcelableExtraCompat<Notification>(EXTRA_NOTIFICATION)
                        ?: run {
                            Log.e(TAG, "POST received without Notification")
                            return
                        }

                val notificationId =
                    intent.getIntExtra(EXTRA_NOTIFICATION_ID, DEFAULT_NOTIFICATION_ID)

                post(
                    context = context,
                    notificationId = notificationId,
                    notification = notification
                )
            }

            ACTION_CANCEL -> {
                val notificationId =
                    intent.getIntExtra(EXTRA_NOTIFICATION_ID, DEFAULT_NOTIFICATION_ID)

                cancel(
                    context = context,
                    notificationId = notificationId
                )
            }
        }
    }

    private fun post(
        context: Context,
        notificationId: Int,
        notification: Notification
    ) {
        if (
            context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "POST_NOTIFICATIONS is not granted")
            return
        }

        Log.d(TAG, "Posting notification $notificationId")
        createNotificationChannelIfNeeded(context)

        NotificationManagerCompat
            .from(context)
            .notify(
                notificationId,
                notification
            )
    }

    private fun cancel(
        context: Context,
        notificationId: Int
    ) {
        Log.d(TAG, "Cancelling notification $notificationId")

        NotificationManagerCompat
            .from(context)
            .cancel(notificationId)
    }

    private fun createNotificationChannelIfNeeded(context: Context) {
        val channelId = "current_event"
        val channelName = "Current Event" // Choose a descriptive name visible to users in system settings
        val importance = NotificationManager.IMPORTANCE_DEFAULT // Adjust importance as needed (e.g., LOW, HIGH)

        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Notifications for ongoing taxi events"
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "NowbarProxy"

        const val ACTION_POST =
            "com.kakao.taxi.action.POST"

        const val ACTION_CANCEL =
            "com.kakao.taxi.action.CANCEL"

        const val EXTRA_NOTIFICATION =
            "com.kakao.taxi.extra.NOTIFICATION"

        const val EXTRA_NOTIFICATION_ID =
            "com.kakao.taxi.extra.NOTIFICATION_ID"

        const val DEFAULT_NOTIFICATION_ID = 1001
    }
}

/**
 * Small compatibility helper so the proxy works on older Android versions
 * without deprecated API warnings everywhere else.
 */
private inline fun <reified T> Intent.getParcelableExtraCompat(
    key: String
): T? {
    return if (android.os.Build.VERSION.SDK_INT >= 33) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key)
    }
}