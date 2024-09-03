package com.plugin.holochainforegroundservice

import android.util.Log
import android.app.Notification
import android.app.Service
import android.content.pm.ServiceInfo
import android.app.ForegroundServiceStartNotAllowedException
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import android.content.Intent
import android.content.Context
import uniffi.holochain_manager_uniffi.HolochainRuntimeFfi
import uniffi.holochain_manager_uniffi.HolochainRuntimeFfiConfig
import uniffi.holochain_manager_uniffi.HolochainRuntimeFfiConfigException
import uniffi.holochain_manager_uniffi.HolochainRuntimeFfiException
import uniffi.holochain_manager_uniffi.AppInfoFfi
import kotlinx.coroutines.runBlocking

class HolochainService : Service() {
    private val isAboveOrEqualAndroid10 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    public var runtime: HolochainRuntimeFfi? = null
    public var admin_port: Int? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(p0: Intent): IBinder? {
        return null
    }

    private fun startForeground() {
        try {
            // Create the notification to display while the service is running
            val notification = NotificationCompat.Builder(this, "HolochainServiceChannel")
                .setContentTitle("Holochain Conductor is Running")
                .build()
            val id = 100
            startForeground(id, notification)

            // start holochain conductor
            // TODO run this in new thread

            val password = byteArrayOf(0x48, 101, 108, 108, 111)
            val config = HolochainRuntimeFfiConfig(
                "https://bootstrap.holo.host",
                "wss://signal.holo.host",
                getFilesDir().toString(),
            )
            runBlocking {
                try {
                    val runtime: HolochainRuntimeFfi = HolochainRuntimeFfi.launch(password, config)
                    Log.d("HolochainService", "runtime 1")

                    val installedApps: List<AppInfoFfi> = runtime.listInstalledApps()
                    Log.d("HolochainService", "installed apps ")
                } catch(e: HolochainRuntimeFfiException) {
                    Log.d("HolochainService", "failed ot launch conductor")
                }
            }
            // admin_port = this.runtime.getAdminPort()
            // Log.d("holochain service", "admin port" + admin_port)

   
            /*
            if (isAboveOrEqualAndroid10) {
                try {
                    ServiceCompat.startForeground(
                        this,
                        id,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                    )
                } catch (e: Exception) {
                    Log.d("HolochainService", "Exception caught at ForegroundServiceHelper.startService: $e")
                }
            } else {
            } */

        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && e is ForegroundServiceStartNotAllowedException) {
                // App not in a valid state to start foreground service
                // (e.g. started from bg)
            }
            // ...
        }
    }

    //public fun listInstalledApps() {
    //    val apps = runBlocking {
    //        this.runtime?.listInstalledApps()
    //    }
//
    //    return apps
    //}

    // public fun getAdminWebsocketPort() {
    //     return this.runtime?.getAdminPort()
    // }
}
