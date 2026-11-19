package com.metrolist.android.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {
    @Test
    fun apkSelectionPicksTheTziakApkByReleaseName() {
        assertEquals(
            "Tziak-Next-14.0.0-tziak.1.apk",
            tziakRelease("v14.0.0-tziak.1", "Tziak-Next-14.0.0-tziak.1.apk", "notes.txt").apk?.name,
        )
        assertEquals("Tziak-14.0.0-tziak.2.apk", tziakRelease("v14.0.0-tziak.2", "Tziak-14.0.0-tziak.2.apk").apk?.name)
        assertEquals("Tziak.apk", tziakRelease("v14.0.0-tziak.3", "Tziak.apk").apk?.name)
    }

    @Test
    fun releaseWithoutAnApkOffersNothingToInstall() {
        assertNull(tziakRelease("v14.0.0-tziak.2", "notes.txt").apk)
        assertNull(tziakRelease("v14.0.0-tziak.2", "Tziak-14.0.0-tziak.1.apk").apk)
        assertNull(tziakRelease("v14.0.0-tziak.2", "Tziak-14.0.0-tziak.2-debug.apk").apk)
        assertNull(tziakRelease("v14.0.0-tziak.2", "unknown.apk").apk)
        val incomplete = tziakRelease("v14.0.0-tziak.2", "Tziak-14.0.0-tziak.2.apk")
        assertNull(incomplete.copy(assets = incomplete.assets.map { it.copy(state = "new") }).apk)
    }

    @Test
    fun onlyReleasesWithAnInstallableApkAreOffered() {
        val noApk = tziakRelease("v14.0.0-tziak.3", "notes.txt")
        val installable = tziakRelease("v14.0.0-tziak.2", "Tziak-14.0.0-tziak.2.apk")
        val current = tziakRelease("v14.0.0-tziak.1", "Tziak-14.0.0-tziak.1.apk")
        assertEquals(installable, AppUpdateManager.newestInstallableUpdate(listOf(noApk, installable, current), "14.0.0-tziak.1"))
        assertNull(AppUpdateManager.newestInstallableUpdate(listOf(noApk, current), "14.0.0-tziak.1"))
        assertNull(AppUpdateManager.newestInstallableUpdate(listOf(installable), "14.0.0-tziak.2"))
    }

    @Test
    fun tziakVersionsCompareNumerically() {
        assertTrue(AppUpdateManager.compareVersions("14.0.0-tziak.2", "14.0.0-tziak.1") > 0)
        assertTrue(AppUpdateManager.compareVersions("14.0.0-tziak.10", "14.0.0-tziak.9") > 0)
        assertTrue(AppUpdateManager.compareVersions("14.0.1-tziak.1", "14.0.0-tziak.9") > 0)
        assertEquals(0, AppUpdateManager.compareVersions("14.0.0-tziak.1", "14.0.0-tziak.1"))
        assertEquals(0, AppUpdateManager.compareVersions("garbage", "14.0.0-tziak.1"))
    }

    @Test
    fun updatesComeFromTheTziakRepoOnly() {
        assertEquals("https://api.github.com/repos/tziakouris/Tziak/releases?per_page=50", AppUpdateManager.RELEASES_URL)
        assertTrue(AppUpdateManager.isTrustedDownloadUrl("https://github.com/tziakouris/Tziak/releases/download/v14.0.0-tziak.2/Tziak.apk"))
        assertFalse(AppUpdateManager.isTrustedDownloadUrl("https://evil.example.com/Tziak.apk"))
        assertFalse(AppUpdateManager.isTrustedDownloadUrl("http://github.com/tziakouris/Tziak/releases/download/v1/Tziak.apk"))
        assertFalse(AppUpdateManager.isTrustedDownloadUrl("https://github.com/MetrolistGroup/Metrolist/releases/download/v14.0.0/Metrolist.apk"))
    }

    private fun tziakRelease(
        tag: String,
        vararg names: String,
    ) = GitHubRelease(
        tagName = tag,
        htmlUrl = "https://github.com/tziakouris/Tziak/releases/tag/$tag",
        assets = names.map { GitHubReleaseAsset(it, "uploaded", 100, downloadUrl = "https://example.com/$it") },
    )

    @Test
    fun apkSignerMustMatchCurrentSignerOrProveForwardRotation() {
        val old = "old".encodeToByteArray()
        val current = "current".encodeToByteArray()
        val next = "next".encodeToByteArray()

        assertTrue(AppUpdateManager.signerDigestsMatch(listOf(current), listOf(current), listOf(old, current)))
        assertTrue(AppUpdateManager.signerDigestsMatch(listOf(current), listOf(next), listOf(old, current, next)))
        assertFalse(AppUpdateManager.signerDigestsMatch(listOf(current), listOf(old), listOf(old)))
        assertFalse(AppUpdateManager.signerDigestsMatch(listOf(current), listOf(next), listOf(next)))
        assertTrue(AppUpdateManager.signerDigestsMatch(listOf(old, current), listOf(current, old), emptyList()))
        assertFalse(AppUpdateManager.signerDigestsMatch(listOf(old, current), listOf(current), listOf(old, current)))
        assertFalse(AppUpdateManager.signerDigestsMatch(emptyList(), listOf(current), listOf(current)))
        assertFalse(AppUpdateManager.signerDigestsMatch(listOf(current), emptyList(), listOf(current)))
        assertFalse(AppUpdateManager.signerDigestsMatch(emptyList(), emptyList(), listOf(current)))
    }
}
