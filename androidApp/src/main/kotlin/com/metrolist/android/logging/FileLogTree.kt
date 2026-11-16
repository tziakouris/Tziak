/*
 * Tziak Project
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.logging

import android.util.Log
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Persists Timber output to a rotating file so it can be shared after a failure. Off by default;
 * nothing is written while [enabled] is false, and turning it off deletes the saved log.
 */
class FileLogTree(
    private val logDir: File,
    private val clock: () -> Long = System::currentTimeMillis,
    private val maxBytes: Long = DEFAULT_MAX_BYTES,
) : Timber.Tree() {
    private val executor = Executors.newSingleThreadExecutor { Thread(it, "FileLogTree").apply { isDaemon = true } }
    private val timeFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)
    private val logFile = File(logDir, LOG_NAME)
    private val oldFile = File(logDir, OLD_NAME)

    @Volatile
    var enabled: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (!value) {
                flush()
                runCatching { logDir.deleteRecursively() }
            }
        }

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        if (!enabled || priority < Log.DEBUG) return
        val line =
            buildString {
                append(synchronized(timeFormat) { timeFormat.format(Date(clock())) })
                append(' ')
                append(
                    when (priority) {
                        Log.DEBUG -> 'D'
                        Log.INFO -> 'I'
                        Log.WARN -> 'W'
                        else -> 'E'
                    },
                )
                append('/')
                append(tag ?: "-")
                append(": ")
                append(message)
                if (t != null) append('\n').append(Log.getStackTraceString(t))
                append('\n')
            }
        runCatching { executor.execute { write(line) } }
    }

    private fun write(line: String) {
        runCatching {
            if (!enabled) return
            logDir.mkdirs()
            if (logFile.length() > maxBytes) {
                oldFile.delete()
                logFile.renameTo(oldFile)
            }
            logFile.appendText(line)
        }
    }

    /** Blocks briefly until queued lines are on disk; call right before the process is killed. */
    fun flush() {
        runCatching { executor.submit {}.get(1, TimeUnit.SECONDS) }
    }

    /** The older and current log joined into one file to share, or null when nothing was recorded. */
    fun shareFile(): File? {
        flush()
        val parts = listOf(oldFile, logFile).filter { it.exists() }
        if (parts.isEmpty()) return null
        return File(logDir, SHARE_NAME).also { share -> share.writeText(parts.joinToString("\n") { it.readText() }) }
    }

    companion object {
        const val DEFAULT_MAX_BYTES = 1_500_000L
        private const val LOG_NAME = "tziak.log"
        private const val OLD_NAME = "tziak.old.log"
        private const val SHARE_NAME = "tziak-debug-log.txt"
    }
}
