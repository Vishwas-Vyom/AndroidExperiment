package me.vishwas.androidexperimental.core.common

import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [toRelativeTime] and [toFormattedDate] extension functions.
 *
 * Dates are computed relative to "now" using offsets so tests never become stale.
 */
class ExtensionsTest {

    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

    private fun nowMinus(millis: Long): String = sdf.format(Date(System.currentTimeMillis() - millis))

    // ── toRelativeTime ────────────────────────────────────────────────────────

    @Test
    fun `toRelativeTime - just now when under 60 seconds old`() {
        val result = nowMinus(30_000).toRelativeTime()  // 30 s ago
        assertEquals("Just now", result)
    }

    @Test
    fun `toRelativeTime - minutes ago when between 1 and 59 minutes old`() {
        val result = nowMinus(5 * 60_000L).toRelativeTime()  // 5 min ago
        assertEquals("5m ago", result)
    }

    @Test
    fun `toRelativeTime - hours ago when between 1 and 23 hours old`() {
        val result = nowMinus(3 * 3_600_000L).toRelativeTime()  // 3 h ago
        assertEquals("3h ago", result)
    }

    @Test
    fun `toRelativeTime - days ago when between 1 and 6 days old`() {
        val result = nowMinus(4 * 86_400_000L).toRelativeTime()  // 4 d ago
        assertEquals("4d ago", result)
    }

    @Test
    fun `toRelativeTime - absolute date for articles older than 7 days`() {
        // > 7 days old → format changes to "MMM dd" style
        val result = nowMinus(10 * 86_400_000L).toRelativeTime()
        // We can't know the exact locale-formatted month name, but it should NOT be "Just now"/Xm/Xh/Xd
        assertFalse(result.endsWith("ago"), "Expected absolute date, got relative: $result")
        assertFalse(result == "Just now", "Expected absolute date, got 'Just now'")
    }

    @Test
    fun `toRelativeTime - returns original string for invalid input`() {
        val invalid = "not-a-date-at-all"
        assertEquals(invalid, invalid.toRelativeTime())
    }

    @Test
    fun `toRelativeTime - returns empty string unchanged`() {
        assertEquals("", "".toRelativeTime())
    }

    // ── toFormattedDate ───────────────────────────────────────────────────────

    @Test
    fun `toFormattedDate - parses valid ISO date and contains year and day`() {
        val isoDate = "2024-06-15T14:30:00Z"
        val result = isoDate.toFormattedDate()
        assertTrue(result.contains("2024"), "Result '$result' should contain the year")
        assertTrue(result.contains("15"), "Result '$result' should contain the day")
        assertTrue(result.contains("14") || result.contains("30"), "Result '$result' should contain time components")
    }

    @Test
    fun `toFormattedDate - returns original string for invalid input`() {
        val invalid = "not-a-date"
        assertEquals(invalid, invalid.toFormattedDate())
    }

    @Test
    fun `toFormattedDate - returns empty string unchanged`() {
        assertEquals("", "".toFormattedDate())
    }

    @Test
    fun `toFormattedDate - result contains bullet separator`() {
        val result = "2024-01-01T09:00:00Z".toFormattedDate()
        assertTrue(result.contains("•"), "Formatted date should contain '•' separator")
    }
}
