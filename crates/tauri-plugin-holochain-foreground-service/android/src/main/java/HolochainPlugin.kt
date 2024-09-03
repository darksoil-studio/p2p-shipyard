package com.plugin.holochainforegroundservice

import android.app.Activity
import android.content.Intent
import android.content.Context
import app.tauri.annotation.Command
import app.tauri.annotation.InvokeArg
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin
import app.tauri.plugin.Invoke
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import android.webkit.WebView

@InvokeArg
class HolochainArgs {
}

@TauriPlugin
class HolochainPlugin(private val activity: Activity): Plugin(activity) {

    override fun load(webView: WebView) {
        super.load(webView)
        launchInternal()
    }

    @Command
    fun launch(invoke: Invoke) {
        val args = invoke.parseArgs(HolochainArgs::class.java)
        launchInternal()
        invoke.resolve()
    }

    private fun launchInternal() {
        val channel = NotificationChannel(
            "HolochainServiceChannel",
            "Holochain Service",
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(activity, HolochainService::class.java)
        activity.startForegroundService(intent)
    }
}
