/*
 * Tziak Project
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.android.logging

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import com.metrolist.android.BuildConfig
import timber.log.Timber
import java.io.File

/** App-wide access to the optional shareable debug log ("Keep debug log" in Privacy settings). */
object DebugLog {
    @Volatile
    private var tree: FileLogTree? = null

    fun install(
        context: Context,
        enabled: Boolean,
    ) {
        val created = FileLogTree(File(context.cacheDir, "debug_logs"))
        tree = created
        Timber.plant(created)
        setEnabled(enabled)
    }

    fun setEnabled(enabled: Boolean) {
        val current = tree ?: return
        if (current.enabled == enabled) return
        current.enabled = enabled
        if (enabled) {
            Timber.i(
                "Tziak ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) on ${Build.MANUFACTURER} ${Build.MODEL}, " +
                    "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}); logging on",
            )
        }
    }

    fun flush() {
        tree?.flush()
    }

    /** A chooser intent that shares the log, or null when nothing has been recorded. */
    fun shareIntent(context: Context): Intent? {
        val file = tree?.shareFile() ?: return null
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
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
