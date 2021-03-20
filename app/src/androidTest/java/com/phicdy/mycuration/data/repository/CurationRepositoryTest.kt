package com.phicdy.mycuration.data.repository

import androidx.test.core.app.ApplicationProvider
import com.phicdy.mycuration.CoroutineTestRule
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.db.DatabaseMigration
import com.phicdy.mycuration.data.db.ResetIconPathTask
import com.phicdy.mycuration.deleteAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CurationRepositoryTest {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var curationRepository: CurationRepository

    private val db = DatabaseHelper(ApplicationProvider.getApplicationContext(), DatabaseMigration(ResetIconPathTask())).writableDatabase

    @Before
    fun setUp() {
        curationRepository = CurationRepository(db, coroutineTestRule.testCoroutineDispatcherProvider, coroutineTestRule.testCoroutineScope)
        deleteAll(db)
    }

    @After
    fun tearDown() {
        deleteAll(db)
    }

    @Test
    fun testSaveNewCuration() = runBlocking {
        val curationId = insertTestCuration()

        val map = curationRepository.getAllCurationWords()
        assertTrue(map.containsKey(curationId))
        assertThat(map.size, `is`(1))
        val addedWords = map[curationId] ?: emptyList<String>()
        val wordSize = 3
        assertEquals(wordSize, addedWords.size)
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
        val curationId = insertTestCuration()

        val map = curationRepository.getAllCurationWords()
        assertEquals(1, map.size)
        assertTrue(map.containsKey(curationId))
        val addedWords1 = map[curationId] ?: emptyList<String>()
        assertEquals(TEST_WORD1, addedWords1[0])
        assertEquals(TEST_WORD2, addedWords1[1])
        assertEquals(TEST_WORD3, addedWords1[2])
    }

    @Test
    fun testGetAllCurationWords() = runBlocking {
        // 2 curations
        val curationId = insertTestCuration()
        val curationName2 = "test2"
        val testWord4 = "word4"
        val testWord5 = "word5"
        val testWord6 = "word6"
        val words2 = ArrayList<String>().apply {
            add(testWord4)
            add(testWord5)
            add(testWord6)
        }
        val curationId2 = curationRepository.store(curationName2, words2).toInt()
        assertTrue(curationId2 > 0)

        val map = curationRepository.getAllCurationWords()
        assertEquals(2, map.size)

        assertTrue(map.containsKey(curationId))
        val addedWords1 = map[curationId] ?: emptyList<String>()
        assertEquals(TEST_WORD1, addedWords1[0])
        assertEquals(TEST_WORD2, addedWords1[1])
        assertEquals(TEST_WORD3, addedWords1[2])

        assertTrue(map.containsKey(curationId2))
        val addedWords2 = map[curationId2] ?: emptyList<String>()
        assertEquals(testWord4, addedWords2[0])
        assertEquals(testWord5, addedWords2[1])
        assertEquals(testWord6, addedWords2[2])
    }

    @Test
    fun testDeleteCuration() = runBlocking {
        val curationId = insertTestCuration()
        assertTrue(curationRepository.delete(curationId))
        assertFalse(curationRepository.isExist(TEST_CURATION_NAME))
    }

    private fun insertTestCuration(): Int = runBlocking {
        val words = ArrayList<String>().apply {
            add(TEST_WORD1)
            add(TEST_WORD2)
            add(TEST_WORD3)
        }
        val id = curationRepository.store(TEST_CURATION_NAME, words).toInt()
        assertTrue(id > 0)
        return@runBlocking id
    }

    companion object {

        private const val TEST_CURATION_NAME = "test"
        private const val TEST_WORD1 = "word1"
        private const val TEST_WORD2 = "word2"
        private const val TEST_WORD3 = "word3"
    }

}