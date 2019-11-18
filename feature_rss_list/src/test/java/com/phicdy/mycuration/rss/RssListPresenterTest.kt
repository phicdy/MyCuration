package com.phicdy.mycuration.rss


import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.entity.Feed
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
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