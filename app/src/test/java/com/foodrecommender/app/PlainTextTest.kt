package com.foodrecommender.app

import com.foodrecommender.app.domain.usecases.PlainText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PlainTextTest {
    @Test
    fun stripsTagsBracketsAndControlCharacters() {
        val raw = "<script>alert(1)</script>\u0000\u0007hello\nthere"
        assertEquals("alert(1)hello there", PlainText.sanitize(raw, 280))
    }

    @Test
    fun capsLengthAfterABoundedScan() {
        val raw = "a".repeat(5_000) + "<b>"
        val cleaned = PlainText.sanitize(raw, 20)
        assertEquals(20, cleaned.length)
        assertFalse(cleaned.contains("<"))
        assertFalse(cleaned.contains(">"))
    }
}
