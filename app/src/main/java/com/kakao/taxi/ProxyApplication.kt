package com.kakao.taxi

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ProxyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val manager = getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Now Bar Proxy",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications forwarded to the Samsung Now Bar"
                setShowBadge(false)
            }
        )
    }

    companion object {
        const val CHANNEL_ID = "nowbar_proxy"
    }
}