package com.metrolist.shared.settings

import com.russhwolf.settings.MapSettings
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class TziakDefaultsTest {
    private val fresh = Settings(MapSettings())

    @Test
    fun audioQualityDefaultsToHigh() = assertEquals("HIGH", fresh.audioQuality)

    @Test
    fun pauseOnMuteDefaultsToOn() = assertTrue(fresh.pauseOnMute)

    @Test
    fun gridItemSizeDefaultsToBig() = assertEquals(GridItemSize.BIG, fresh.gridItemSize)

    @Test
    fun autoPlaylistsAreOffExceptDownloaded() {
        assertFalse(fresh.showLikedPlaylist)
        assertFalse(fresh.showTopPlaylist)
        assertFalse(fresh.showUploadedPlaylist)
        assertFalse(fresh.showCachedPlaylist)
        assertTrue(fresh.showDownloadedPlaylist)
    }

    @Test
    fun updateCheckDefaultsToOn() = assertTrue(fresh.checkForUpdates)

    @Test
    fun playerLookDefaultsMatchTheChosenStyle() {
        assertEquals(PlayerThumbnailStyle.FULLSCREEN, fresh.playerThumbnailStyle)
        assertEquals(PlayerBackgroundStyle.ANIMATED_GRADIENT, fresh.playerBackgroundStyle)
        assertTrue(fresh.immersivePlayer)
        assertTrue(fresh.useRoundedRectPlayButton)
        assertTrue(fresh.enableAnimatedCovers)
        assertTrue(fresh.enableAppleMusicArtistImages)
        assertEquals(AnimatedCoverResolution.ORIGINAL, fresh.animatedCoverResolution)
        assertEquals(2, fresh.libraryPinchColumns)
    }

    @Test
    fun musicVideoButtonIsAvailable() = assertTrue(fresh.musicVideoPlaybackEnabled)
}
