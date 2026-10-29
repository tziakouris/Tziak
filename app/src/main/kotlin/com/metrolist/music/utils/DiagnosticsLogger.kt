/**
 * Tziak Project
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Debug
import android.os.PowerManager
import android.os.Process
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Periodically writes battery, temperature, thermal and CPU figures to the debug log so battery
 * drain and heat can be diagnosed from a shared log. Costs one cheap snapshot every few minutes.
 */
object DiagnosticsLogger {
    private const val TAG = "Diagnostics"
    private const val INTERVAL_MS = 5 * 60 * 1000L

    private var job: Job? = null
    private var thermalRegistered = false
    private var lastWallMs = 0L
    private var lastCpuMs = 0L
    private var lastLevel = -1
    private var lastLevelAtMs = 0L

    fun start(
        context: Context,
        scope: CoroutineScope,
        playbackSummary: () -> String,
    ) {
        if (job?.isActive == true) return
        val app = context.applicationContext
        registerThermalListener(app)
        lastWallMs = SystemClock.elapsedRealtime()
        lastCpuMs = Process.getElapsedCpuTime()
        job =
            scope.launch(Dispatchers.Default) {
                while (isActive) {
                    if (!FileLogTree.enabled) {
                        delay(INTERVAL_MS)
                        continue
                    }
                    val playback = withContext(Dispatchers.Main) { runCatching(playbackSummary).getOrDefault("?") }
                    runCatching { snapshot(app, playback) }.onFailure { Timber.tag(TAG).w(it, "snapshot failed") }
                    delay(INTERVAL_MS)
                }
            }
    }

    private fun snapshot(
        context: Context,
        playback: String,
    ) {
        val now = SystemClock.elapsedRealtime()
        val cpuNow = Process.getElapsedCpuTime()
        val cpuPercent = if (now > lastWallMs) (cpuNow - lastCpuMs) * 100.0 / (now - lastWallMs) else 0.0
        lastWallMs = now
        lastCpuMs = cpuNow

        val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val percent = if (level >= 0 && scale > 0) level * 100 / scale else -1
        val tempC = (battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10.0
        val voltage = battery?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val plugged = (battery?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0) != 0

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentMa = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000

        val drain =
            if (!plugged && lastLevel >= 0 && percent >= 0 && now > lastLevelAtMs) {
                (lastLevel - percent) * 3_600_000.0 / (now - lastLevelAtMs)
            } else {
                null
            }
        if (percent >= 0) {
            lastLevel = percent
            lastLevelAtMs = now
        }

        val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) thermalName(power.currentThermalStatus) else "n/a"
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        val net =
            when {
                caps == null -> "none"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "bluetooth"
                else -> "other"
            }
        val heapMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1_048_576
        val nativeMb = Debug.getNativeHeapAllocatedSize() / 1_048_576

        Timber.tag(TAG).i(
            "battery=%d%% temp=%.1fC %s current=%dmA voltage=%dmV drain=%s thermal=%s powersave=%b screen=%b " +
                "appCpu=%.1f%% heap=%dMB native=%dMB net=%s | %s",
            percent,
            tempC,
            if (plugged) "charging" else "on-battery",
            currentMa,
            voltage,
            drain?.let { "%.1f%%/h".format(it) } ?: "n/a",
            thermal,
            power.isPowerSaveMode,
            power.isInteractive,
            cpuPercent,
            heapMb,
            nativeMb,
            net,
            playback,
        )
    }

    private fun registerThermalListener(context: Context) {
        if (thermalRegistered || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        thermalRegistered = true
        val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        runCatching {
            power.addThermalStatusListener { status ->
                Timber.tag(TAG).w("thermal status changed: %s", thermalName(status))
            }
        }
    }

    private fun thermalName(status: Int): String =
        when (status) {
            PowerManager.THERMAL_STATUS_NONE -> "none"
            PowerManager.THERMAL_STATUS_LIGHT -> "light"
            PowerManager.THERMAL_STATUS_MODERATE -> "moderate"
            PowerManager.THERMAL_STATUS_SEVERE -> "SEVERE"
            PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"
            PowerManager.THERMAL_STATUS_EMERGENCY -> "EMERGENCY"
            PowerManager.THERMAL_STATUS_SHUTDOWN -> "SHUTDOWN"
            else -> "unknown($status)"
        }
}
