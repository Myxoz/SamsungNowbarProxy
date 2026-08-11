package com.kakao.taxi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.content.pm.PackageManager
import android.widget.TextView
import androidx.core.app.ActivityCompat

class MainActivity : Activity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this).apply {
            text = """
                Now Bar Proxy

                Package:
                ${packageName}

                This app forwards notifications to Samsung's
                Live Notification / Now Bar system.

                No background service is required.
            """.trimIndent()

            setPadding(48, 48, 48, 48)
        }

        setContentView(text)

        if (
            android.os.Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }
    }
}