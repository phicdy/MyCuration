package com.phicdy.mycuration.data.repository

import android.support.test.InstrumentationRegistry.getTargetContext
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.db.DatabaseHelper
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CurationRepositoryTest {

    private lateinit var curationRepository: CurationRepository
    private lateinit var adapter: DatabaseAdapter

    @Before
    fun setUp() {
        DatabaseAdapter.setUp(DatabaseHelper(getTargetContext()))
        val db = DatabaseHelper(getTargetContext()).writableDatabase
        curationRepository = CurationRepository(db)
        adapter = DatabaseAdapter.getInstance()
        adapter.deleteAll()
    }

    @After
    fun tearDown() {
        adapter.deleteAll()
    }

    @Test
    fun testSaveNewCuration() = runBlocking {
        insertTestCuration()

        val curationId = adapter.getCurationIdByName(TEST_CURATION_NAME)
        val map = curationRepository.getAllCurationWords()
        assertTrue(map.containsKey(curationId))
        assertThat(map.size, `is`(1))
        val addedWords = map.get(curationId) ?: emptyList<String>()
        val TEST_WORDS_SIZE = 3
        assertEquals(TEST_WORDS_SIZE, addedWords.size)
        assertEquals(TEST_WORD1, addedWords[0])
        assertEquals(TEST_WORD2, addedWords[1])
        assertEquals(TEST_WORD3, addedWords[2])
    }

    @Test
    fun whenDefault_ThenEmptyWordsReturn() = runBlocking {
        val map = curationRepository.getAllCurationWords()
        assertEquals(0, map.size)
    }

    @Test
    fun whenInsert1Curation_ThenReturnTheWords() = runBlocking {
        // 1 curation
        insertTestCuration()
        val curationId1 = adapter.getCurationIdByName(TEST_CURATION_NAME)

        val map = curationRepository.getAllCurationWords()
        assertEquals(1, map.size)
        assertTrue(map.containsKey(curationId1))
        val addedWords1 = map.get(curationId1) ?: emptyList<String>()
        assertEquals(TEST_WORD1, addedWords1[0])
        assertEquals(TEST_WORD2, addedWords1[1])
        assertEquals(TEST_WORD3, addedWords1[2])
    }

    @Test
    fun testGetAllCurationWords() = runBlocking {
        // 2 curations
        insertTestCuration()
        val curationName2 = "test2"
        val testWord4 = "word4"
        val testWord5 = "word5"
        val testWord6 = "word6"
        val words2 = ArrayList<String>().apply {
            add(testWord4)
            add(testWord5)
            add(testWord6)
        }
        assertTrue(adapter.saveNewCuration(curationName2, words2))
        val curationId2 = adapter.getCurationIdByName(curationName2)

        val map = curationRepository.getAllCurationWords()
        assertEquals(2, map.size)

        val curationId1 = adapter.getCurationIdByName(TEST_CURATION_NAME)
        assertTrue(map.containsKey(curationId1))
        val addedWords1 = map.get(curationId1) ?: emptyList<String>()
        assertEquals(TEST_WORD1, addedWords1[0])
        assertEquals(TEST_WORD2, addedWords1[1])
        assertEquals(TEST_WORD3, addedWords1[2])

        assertTrue(map.containsKey(curationId2))
        val addedWords2 = map.get(curationId2) ?: emptyList<String>()
        assertEquals(testWord4, addedWords2[0])
        assertEquals(testWord5, addedWords2[1])
        assertEquals(testWord6, addedWords2[2])
    }

    private fun insertTestCuration() {
        val words = ArrayList<String>().apply {
            add(TEST_WORD1)
            add(TEST_WORD2)
            add(TEST_WORD3)
        }
        assertTrue(adapter.saveNewCuration(TEST_CURATION_NAME, words))
    }

    companion object {

        private const val TEST_CURATION_NAME = "test"
        private const val TEST_WORD1 = "word1"
        private const val TEST_WORD2 = "word2"
        private const val TEST_WORD3 = "word3"
    }

}