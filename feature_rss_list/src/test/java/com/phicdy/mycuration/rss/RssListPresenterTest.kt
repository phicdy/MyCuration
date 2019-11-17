package com.phicdy.mycuration.rss


import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString

class RssListPresenterTest {

    private lateinit var presenter: RssListPresenter
    private val view = mock<RssListView>()
    private val mockPref = mock<SharedPreferences>()
    private val mockRssRepository = mock<RssRepository>()
    private val networkTaskManager = mock<NetworkTaskManager>()
    private lateinit var allFeeds: ArrayList<Feed>

    @Before
    fun setup() {
        // Mock two RSS returns
        val firstRss = Feed(FIRST_RSS_ID, FIRST_RSS_TITLE, "", Feed.DEDAULT_ICON_PATH, "", FIRST_RSS_UNREAD_COUNT, "")
        val secondRss = Feed(SECOND_RSS_ID, SECOND_RSS_TITLE, "", SECOND_RSS_ICON_PATH, "", SECOND_RSS_UNREAD_COUNT, "")
        allFeeds = arrayListOf(firstRss, secondRss)
        runBlocking {
            whenever(mockRssRepository.getAllFeedsWithNumOfUnreadArticles()).thenReturn(allFeeds)
            whenever(mockRssRepository.getNumOfRss()).thenReturn(2)
        }

        // Set up mock PreferenceHelper
        val mockContext = mock<Context>()
        whenever(mockContext.getSharedPreferences("FilterPref", Context.MODE_PRIVATE)).thenReturn(mockPref)
        PreferenceHelper.setUp(mockContext)
        val mockEdit = mock<SharedPreferences.Editor>()
        whenever(mockPref.edit()).thenReturn(mockEdit)
        whenever(mockEdit.putLong(any(), any())).thenReturn(mockEdit)

        presenter = RssListPresenter(view, PreferenceHelper, mockRssRepository, networkTaskManager)
    }

    @Test
    fun `when onCreateView and RSS doesn't exist then hide recyclerview`() = runBlocking {
        whenever(mockRssRepository.getNumOfRss()).thenReturn(0)
        presenter.onCreateView()
        verify(view, times(1)).hideRecyclerView()
    }

    @Test
    fun `when onCreateView and RSS doesn't exist then show empty view`() = runBlocking {
        whenever(mockRssRepository.getNumOfRss()).thenReturn(0)
        presenter.onCreateView()
        verify(view, times(1)).showEmptyView()
    }

    @Test
    fun `when onCreateView and RSS exist then show recyclerview`() = runBlocking {
        presenter.onCreateView()
        verify(view, times(1)).showRecyclerView()
    }

    @Test
    fun `when onCreateView and RSS exist then hide empty view`() = runBlocking {
        presenter.onCreateView()
        verify(view, times(1)).hideEmptyView()
    }

    @Test
    fun `when onCreateView and RSS exist then fetch RSS from database`() {
        runBlocking {
            presenter.onCreateView()
            verify(mockRssRepository).getAllFeedsWithNumOfUnreadArticles()
        }
    }

    @Test
    fun `when onCreateView and RSS exist then init with hidden list`() = runBlocking {
        presenter.onCreateView()
        verify(view, times(1)).init(presenter.unreadOnlyFeeds.toRssListItem(RssListFooterState.UNREAD_ONLY))
    }

    @Test
    fun `when onCreateView and show all RSS and onCreateView then init with all list`() = runBlocking {
        presenter.onCreateView()
        presenter.onRssFooterClicked() // call init(allFeeds)
        presenter.pause()
        presenter.onCreateView()
        verify(view, times(2)).init(presenter.allFeeds.toRssListItem(RssListFooterState.ALL))
    }

    @Test
    fun `when onCreateView and RSS exist and auto update in main UI is enabled and after interval then show refreshing view`() = runBlocking {
        whenever(mockPref.getBoolean(anyString(), any())).thenReturn(true)
        whenever(mockPref.getLong(anyString(), anyLong())).thenReturn(System.currentTimeMillis() - 1000 * 60)
        presenter.onCreateView()
        verify(view, times(1)).setRefreshing(true)
    }

    @Test
    fun `when onCreateView and RSS exist and auto update in main UI is enabled and before interval then show refreshing view`() = runBlocking {
        whenever(mockPref.getBoolean(anyString(), any())).thenReturn(true)
        whenever(mockPref.getLong(anyString(), anyLong())).thenReturn(System.currentTimeMillis())
        presenter.onCreateView()
        verify(view, times(0)).setRefreshing(true)
    }

    @Test
    fun `when onCreateView and RSS exist and auto update in main UI is disabled then not show refreshing view`() = runBlocking {
        whenever(mockPref.getBoolean(anyString(), any())).thenReturn(false)
        presenter.onCreateView()
        verify(view, times(0)).setRefreshing(true)
    }

    @Test
    fun `when first RSS is hidden then first RSS title will be second RSS`() = runBlocking {
        presenter.onCreateView()
        assertThat(presenter.unreadOnlyFeeds)
                .hasSize(1)
                .extracting("title")
                .contains(SECOND_RSS_TITLE)
        return@runBlocking
    }

    @Test
    fun `when all of articles were read then show all of RSS`() = runBlocking {
        val firstRss = Feed(FIRST_RSS_ID, FIRST_RSS_TITLE, "", "", "", 0, "")
        val secondRss = Feed(SECOND_RSS_ID, SECOND_RSS_TITLE, "", "", "", 0, "")
        val alreadyReadRss = arrayListOf(firstRss, secondRss)
        whenever(mockRssRepository.getAllFeedsWithNumOfUnreadArticles()).thenReturn(alreadyReadRss)
        presenter.onCreateView()
        assertThat(presenter.unreadOnlyFeeds)
                .hasSize(2)
                .extracting("title")
                .contains(FIRST_RSS_TITLE, SECOND_RSS_TITLE)
        return@runBlocking
    }

    @Test
    fun `when delete ok button is clicked in hidden status and succeeds then delete the RSS`() = runBlocking {
        whenever(mockRssRepository.deleteRss(anyInt())).thenReturn(true)
        presenter.onCreateView() // init list
        presenter.removeRss(FIRST_RSS_ID)
        // Current status is hidden and size is 1, so hidden list becomes all RSS list after refresh
        assertThat(presenter.unreadOnlyFeeds)
                .hasSize(1)
                .extracting("id")
                .contains(SECOND_RSS_ID)
        assertThat(presenter.allFeeds)
                .hasSize(1)
                .extracting("id")
                .contains(SECOND_RSS_ID)
        return@runBlocking
    }

    @Test
    fun `when delete ok button is clicked in all of RSS and succeeds then delete the RSS`() = runBlocking {
        whenever(mockRssRepository.deleteRss(anyInt())).thenReturn(true)
        presenter.onCreateView() // init list
        presenter.onRssFooterClicked() // Change to all RSS
        presenter.removeRss(FIRST_RSS_ID)
        // Current status is all and first RSS status is read, so hidden list has no update
        assertThat(presenter.unreadOnlyFeeds)
                .hasSize(1)
                .extracting("id")
                .contains(SECOND_RSS_ID)
        assertThat(presenter.allFeeds)
                .hasSize(1)
                .extracting("id")
                .contains(SECOND_RSS_ID)
        return@runBlocking
    }

    @Test
    fun `when delete all of RSS then show empty view`() = runBlocking {
        whenever(mockRssRepository.deleteRss(anyInt())).thenReturn(true)
        presenter.onCreateView() // init list
        presenter.onRssFooterClicked() // Change to all RSS
        presenter.removeRss(FIRST_RSS_ID)
        presenter.removeRss(SECOND_RSS_ID)
        verify(view).showEmptyView()
    }

    @Test
    fun `when refresh and RSS is empty then finish refresh`() = runBlocking {
        whenever(mockRssRepository.getAllFeedsWithNumOfUnreadArticles()).thenReturn(arrayListOf())
        presenter.onCreateView()
        presenter.onRefresh()
        verify(view, times(1)).onRefreshCompleted()
    }

    @Test
    fun `when finish refresh then hide refresh view`() = runBlocking {
        presenter.onFinishUpdate()
        verify(view, times(1)).onRefreshCompleted()
    }

    @Test
    fun `when finish refresh then fetch RSS`() {
        runBlocking {
            presenter.onFinishUpdate()
            verify(mockRssRepository).getAllFeedsWithNumOfUnreadArticles()
        }
    }

    @Test
    fun `when finish refresh then reload RSS list`() {
        runBlocking { presenter.onFinishUpdate() }
        verify(view, times(1)).init(presenter.unreadOnlyFeeds.toRssListItem(RssListFooterState.UNREAD_ONLY))
    }

    @Test
    fun `when finish refresh then last update time will be updated`() {
        runBlocking { presenter.onFinishUpdate() }
        verify(mockPref.edit(), times(1)).putLong(anyString(), anyLong())
    }

    @Test
    fun `when click footer twice then go back to hidden status`() {
        runBlocking {
            presenter.onCreateView()
            presenter.onRssFooterClicked()
            presenter.onRssFooterClicked()
        }
        argumentCaptor<List<RssListItem>> {
            verify(view, times(3)).init(capture())
            assertEquals(presenter.unreadOnlyFeeds.toRssListItem(RssListFooterState.UNREAD_ONLY), firstValue)
            assertEquals(presenter.allFeeds.toRssListItem(RssListFooterState.ALL), secondValue)
            assertEquals(presenter.unreadOnlyFeeds.toRssListItem(RssListFooterState.UNREAD_ONLY), thirdValue)
        }
    }

    private fun ArrayList<Feed>.toRssListItem(state: RssListFooterState): List<RssListItem> = mutableListOf<RssListItem>().apply {
        add(RssListItem.All(this@toRssListItem.sumBy { it.unreadAriticlesCount }))
        add(RssListItem.Favroite)
        this@toRssListItem.map {
            this.add(RssListItem.Content(
                    rssId = it.id,
                    rssTitle = it.title,
                    rssIconPath = it.iconPath,
                    isDefaultIcon = it.iconPath.isBlank() || it.iconPath == Feed.DEDAULT_ICON_PATH,
                    unreadCount = it.unreadAriticlesCount
            ))
        }
        add(RssListItem.Footer(state))
    }

    companion object {

        private const val FIRST_RSS_TITLE = "rss1"
        private const val SECOND_RSS_TITLE = "rss2"
        private const val FIRST_RSS_ID = 0
        private const val SECOND_RSS_ID = 1
        private const val SECOND_RSS_ICON_PATH = "https://www.google.com/icon"
        private const val FIRST_RSS_UNREAD_COUNT = 0
        private const val SECOND_RSS_UNREAD_COUNT = 1
    }
}