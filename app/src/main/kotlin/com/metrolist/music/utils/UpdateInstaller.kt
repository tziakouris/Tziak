/**
 * Tziak Project
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import com.metrolist.music.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** Downloads a release APK from this project's GitHub releases and hands it to Android's installer. */
object UpdateInstaller {
    private const val TAG = "UpdateInstaller"
    private const val ALLOWED_PREFIX = "https://github.com/tziakouris/Tziak/releases/download/"

    fun downloadAndInstall(
        context: Context,
        scope: CoroutineScope,
        url: String,
        expectedSha256: String,
    ) {
        val app = context.applicationContext
        if (!url.startsWith(ALLOWED_PREFIX)) {
            Timber.tag(TAG).w("Refusing to download update from unexpected URL")
            toast(app, R.string.update_download_failed)
            return
        }
        if (!app.packageManager.canRequestPackageInstalls()) {
            toast(app, R.string.update_install_permission)
            app.startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${app.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            return
        }
        scope.launch(Dispatchers.IO) {
            toastOnMain(app, R.string.update_downloading)
            val file = File(File(app.cacheDir, "updates"), "Tziak-update.apk")
            try {
                download(url, file)
                if (expectedSha256.isNotEmpty() && sha256(file) != expectedSha256.lowercase()) {
                    file.delete()
                    Timber.tag(TAG).w("Update checksum mismatch; discarded download")
                    toastOnMain(app, R.string.update_checksum_failed)
                    return@launch
                }
                val uri = FileProvider.getUriForFile(app, "${app.packageName}.FileProvider", file)
                val install =
                    Intent(Intent.ACTION_VIEW)
                        .setDataAndType(uri, "application/vnd.android.package-archive")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                withContext(Dispatchers.Main) { app.startActivity(install) }
                Timber.tag(TAG).i("Update downloaded; installer opened")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Update download failed")
                file.delete()
                toastOnMain(app, R.string.update_download_failed)
            }
        }
    }

    private fun download(
        url: String,
        target: File,
    ) {
        target.parentFile?.mkdirs()
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        try {
            check(connection.responseCode == 200) { "HTTP ${connection.responseCode}" }
            connection.inputStream.use { input -> target.outputStream().use { input.copyTo(it) } }
        } finally {
            connection.disconnect()
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun toast(
        context: Context,
        message: Int,
    ) = Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    private suspend fun toastOnMain(
        context: Context,
        message: Int,
    ) = withContext(Dispatchers.Main) { toast(context, message) }
}
