package com.metrolist.android.playback

import android.os.SystemClock
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.settings.Settings
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Debug-log aid (only active while "Keep debug log" is on): records which songs the player asks for, by
 * artist and title, and how long each took to resolve and to start playing.
 */
internal object SongStartTimer {
    private const val TAG = "SongStart"
    private const val MAX_PENDING = 64

    private class Pending(
        val label: String,
        val beginMs: Long,
        var resolvedMs: Long? = null,
        var source: String = "network",
    )

    private val pending = ConcurrentHashMap<String, Pending>()

    private fun enabled() = Settings.shared.keepDebugLog

    internal fun label(song: SongItem): String = "${song.artists.joinToString { it.name }} - ${song.title}"

    fun begin(song: SongItem) {
        if (!enabled()) return
        if (pending.size >= MAX_PENDING) pending.clear()
        pending[song.id] = Pending(label(song), SystemClock.elapsedRealtime())
        Timber.tag(TAG).d("requested id=%s song=%s", song.id, label(song))
    }

    fun resolved(
        id: String,
        source: String,
    ) {
        if (!enabled()) return
        val entry = pending[id] ?: return
        entry.resolvedMs = SystemClock.elapsedRealtime() - entry.beginMs
        entry.source = source
    }

    fun playing(videoId: String?) {
        if (!enabled() || videoId == null) return
        val entry = pending.remove(videoId) ?: return
        val total = SystemClock.elapsedRealtime() - entry.beginMs
        Timber.tag(TAG).d(
            "started id=%s song=%s source=%s resolveMs=%s totalMs=%d",
            videoId,
            entry.label,
            entry.source,
            entry.resolvedMs?.toString() ?: "n/a",
            total,
        )
    }
}
