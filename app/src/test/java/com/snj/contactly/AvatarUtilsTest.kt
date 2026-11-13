package com.snj.contactly

import com.snj.contactly.utils.AvatarUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarUtilsTest {

    @Test
    fun `extractInitials handles single multi-word and empty names`() {
        assertEquals("AS", AvatarUtils.extractInitials("Aarav Sharma"))
        assertEquals("M", AvatarUtils.extractInitials("Maya"))
        assertEquals("JD", AvatarUtils.extractInitials("John Michael Doe"))
        assertEquals("?", AvatarUtils.extractInitials("   "))
    }

    @Test
    fun `paletteFor returns deterministic palette for equivalent names`() {
        val first = AvatarUtils.paletteFor("Clara Bennett")
        val second = AvatarUtils.paletteFor("  clara bennett ")
        assertEquals(first, second)
    }
}

