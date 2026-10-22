/**
 * Tziak Project
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.metrolist.music.BuildConfig
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Persists Timber output to a rotating file so a user can share it after a failure
 * (e.g. Android Auto problems that cannot be reproduced with a debugger attached).
 */
class FileLogTree private constructor(
    private val logFile: File,
) : Timber.Tree() {
    private val executor = Executors.newSingleThreadExecutor { Thread(it, "FileLogTree").apply { isDaemon = true } }
    private val timeFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        if (priority < Log.DEBUG) return
        val line =
            buildString {
                append(timeFormat.format(Date()))
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
            logFile.parentFile?.mkdirs()
            if (logFile.length() > MAX_BYTES) {
                val old = File(logFile.parentFile, OLD_NAME)
                old.delete()
                logFile.renameTo(old)
            }
            logFile.appendText(line)
        }
    }

    companion object {
        private const val LOG_NAME = "tziak.log"
        private const val OLD_NAME = "tziak.old.log"
        private const val SHARE_NAME = "tziak-debug-log.txt"
        private const val MAX_BYTES = 1_500_000L

        @Volatile
        private var instance: FileLogTree? = null

        fun install(context: Context) {
            val tree = FileLogTree(File(File(context.cacheDir, "logs"), LOG_NAME))
            instance = tree
            Timber.plant(tree)
            Timber.i(
                "Tziak ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) on ${Build.MANUFACTURER} ${Build.MODEL}, " +
                    "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}); process started",
            )
        }

        /** Blocks briefly until queued lines are on disk; call right before the process is killed. */
        fun flush() {
            val tree = instance ?: return
            runCatching { tree.executor.submit {}.get(1, TimeUnit.SECONDS) }
        }

        fun createShareIntent(context: Context): Intent? {
            flush()
            val dir = File(context.cacheDir, "logs")
            val parts = listOf(File(dir, OLD_NAME), File(dir, LOG_NAME)).filter { it.exists() }
            if (parts.isEmpty()) return null
            val shareFile = File(dir, SHARE_NAME)
            shareFile.writeText(parts.joinToString("\n") { it.readText() })
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.FileProvider", shareFile)
            val send =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Tziak debug log")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            return Intent.createChooser(send, null)
        }
    }
}
