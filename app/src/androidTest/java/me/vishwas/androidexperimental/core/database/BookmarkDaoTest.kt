package me.vishwas.androidexperimental.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit4.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.vishwas.androidexperimental.util.testBookmarkEntity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Instrumented tests for [BookmarkDao] using a real in-memory Room database.
 *
 * Per project policy: no mock databases. Every test runs against an actual Room instance.
 * The database is created fresh for each test and closed in [teardown].
 */
@RunWith(AndroidJUnit4::class)
class BookmarkDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: me.vishwas.androidexperimental.core.database.dao.BookmarkDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.bookmarkDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // ── insert ────────────────────────────────────────────────────────────────

    @Test
    fun insert_and_retrieve_single_bookmark() = runTest {
        val entity = testBookmarkEntity()
        dao.insert(entity)

        val result = dao.getBookmarks().first()
        assertEquals(1, result.size)
        assertEquals(entity, result[0])
    }

    @Test
    fun insert_with_same_url_replaces_existing_entry() = runTest {
        val original = testBookmarkEntity(title = "Original")
        val updated = testBookmarkEntity(title = "Updated")

        dao.insert(original)
        dao.insert(updated)  // OnConflictStrategy.REPLACE

        val result = dao.getBookmarks().first()
        assertEquals(1, result.size, "Expected only one entry after replace")
        assertEquals("Updated", result[0].title)
    }

    @Test
    fun insert_multiple_unique_bookmarks() = runTest {
        dao.insert(testBookmarkEntity(url = "url1"))
        dao.insert(testBookmarkEntity(url = "url2"))
        dao.insert(testBookmarkEntity(url = "url3"))

        val result = dao.getBookmarks().first()
        assertEquals(3, result.size)
    }

    // ── getBookmarks ordering ─────────────────────────────────────────────────

    @Test
    fun getBookmarks_returns_entries_ordered_by_publishedAt_descending() = runTest {
        dao.insert(testBookmarkEntity(url = "url1", publishedAt = "2024-01-01T10:00:00Z"))
        dao.insert(testBookmarkEntity(url = "url2", publishedAt = "2024-06-01T10:00:00Z"))
        dao.insert(testBookmarkEntity(url = "url3", publishedAt = "2024-03-01T10:00:00Z"))

        val result = dao.getBookmarks().first()

        // Descending order → newest first
        assertEquals("url2", result[0].url)
        assertEquals("url3", result[1].url)
        assertEquals("url1", result[2].url)
    }

    @Test
    fun getBookmarks_returns_empty_list_when_table_is_empty() = runTest {
        val result = dao.getBookmarks().first()
        assertTrue(result.isEmpty())
    }

    // ── isBookmarked ──────────────────────────────────────────────────────────

    @Test
    fun isBookmarked_returns_true_for_existing_url() = runTest {
        dao.insert(testBookmarkEntity(url = "https://example.com/article"))

        val result = dao.isBookmarked("https://example.com/article").first()
        assertTrue(result)
    }

    @Test
    fun isBookmarked_returns_false_for_unknown_url() = runTest {
        val result = dao.isBookmarked("https://nonexistent.com/article").first()
        assertFalse(result)
    }

    @Test
    fun isBookmarked_updates_to_false_after_deletion() = runTest {
        val url = "https://example.com/article"
        dao.insert(testBookmarkEntity(url = url))

        assertTrue(dao.isBookmarked(url).first())

        dao.delete(url)

        assertFalse(dao.isBookmarked(url).first())
    }

    // ── countBookmarked ───────────────────────────────────────────────────────

    @Test
    fun countBookmarked_returns_1_for_existing_bookmark() = runTest {
        dao.insert(testBookmarkEntity(url = "url1"))
        assertEquals(1, dao.countBookmarked("url1"))
    }

    @Test
    fun countBookmarked_returns_0_for_missing_bookmark() = runTest {
        assertEquals(0, dao.countBookmarked("url-that-does-not-exist"))
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    fun delete_removes_the_bookmark_with_matching_url() = runTest {
        dao.insert(testBookmarkEntity(url = "url1"))
        dao.insert(testBookmarkEntity(url = "url2"))
        dao.delete("url1")

        val result = dao.getBookmarks().first()
        assertEquals(1, result.size)
        assertEquals("url2", result[0].url)
    }

    @Test
    fun delete_is_idempotent_for_nonexistent_url() = runTest {
        dao.insert(testBookmarkEntity())
        // Deleting a url that was never inserted should not throw or remove other entries
        dao.delete("does-not-exist")

        val result = dao.getBookmarks().first()
        assertEquals(1, result.size)
    }

    @Test
    fun delete_all_bookmarks_results_in_empty_list() = runTest {
        dao.insert(testBookmarkEntity(url = "url1"))
        dao.insert(testBookmarkEntity(url = "url2"))
        dao.delete("url1")
        dao.delete("url2")

        assertTrue(dao.getBookmarks().first().isEmpty())
    }
}
