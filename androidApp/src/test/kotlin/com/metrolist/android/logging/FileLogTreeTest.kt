package com.metrolist.android.logging

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

class FileLogTreeTest {
    @get:Rule val temporary = TemporaryFolder()

    private fun tree(maxBytes: Long = 1_500_000L) = FileLogTree(temporary.newFolder("logs"), maxBytes = maxBytes)

    private fun read(tree: FileLogTree): String {
        val file = tree.shareFile()
        assertNotNull(file)
        return file!!.readText()
    }

    @Test
    fun writesNothingWhenDisabled() {
        val tree = tree()
        tree.i("hello")
        tree.flush()
        assertNull(tree.shareFile())
    }

    @Test
    fun writesLinesWhenEnabled() {
        val tree = tree()
        tree.enabled = true
        tree.i("hello")
        tree.flush()
        assertTrue(read(tree).contains("hello"))
    }

    @Test
    fun turningItOffDeletesTheLog() {
        val tree = tree()
        tree.enabled = true
        tree.i("hello")
        tree.flush()
        tree.enabled = false
        assertNull(tree.shareFile())
        tree.enabled = true
        tree.i("after")
        tree.flush()
        val text = read(tree)
        assertTrue(text.contains("after"))
        assertFalse(text.contains("hello"))
    }

    @Test
    fun rotatesPastMaxSizeAndKeepsBothParts() {
        val tree = tree(maxBytes = 200)
        tree.enabled = true
        repeat(30) { tree.i("line number $it") }
        tree.flush()
        val text = read(tree)
        assertTrue(text.contains("line number 29"))
        assertTrue("old lines must be dropped, got ${text.length} chars", text.length < 30 * 40)
    }
}
