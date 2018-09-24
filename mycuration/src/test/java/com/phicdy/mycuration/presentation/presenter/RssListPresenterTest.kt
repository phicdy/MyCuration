package com.phicdy.mycuration.presentation.presenter


import android.content.Context
import android.content.SharedPreferences
import com.phicdy.mycuration.data.db.DatabaseAdapter
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.rss.UnreadCountManager
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.view.RssListView
import com.phicdy.mycuration.presentation.view.fragment.RssListFragment
import com.phicdy.mycuration.util.PreferenceHelper
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.ArrayList

class RssListPresenterTest {

    private lateinit var presenter: RssListPresenter
    private lateinit var view: MockView
    private val adapter = Mockito.mock(DatabaseAdapter::class.java)

    @Before
    fun setup() {
        DatabaseAdapter.inject(adapter)
        val firstRss = Feed(FIRST_RSS_ID, FIRST_RSS_TITLE, "", "", "", 0, "")
        val secondRss = Feed(SECOND_RSS_ID, SECOND_RSS_TITLE, "", "", "", 1, "")
        val allFeeds = arrayListOf(firstRss, secondRss)
        Mockito.`when`(adapter.allFeedsWithNumOfUnreadArticles).thenReturn(allFeeds)
        val networkTaskManager = NetworkTaskManager

        val mockContext = Mockito.mock(Context::class.java)
        Mockito.`when`(mockContext.getSharedPreferences("FilterPref", Context.MODE_PRIVATE))
                .thenReturn(Mockito.mock(SharedPreferences::class.java))
        PreferenceHelper.setUp(mockContext)
        val preferenceHelper = PreferenceHelper

        UnreadCountManager.addFeed(firstRss)
        UnreadCountManager.addFeed(secondRss)
        view = MockView()
        presenter = RssListPresenter(view, preferenceHelper, adapter, networkTaskManager, UnreadCountManager)
    }

    @After
    fun tearDown() {
        UnreadCountManager.clear()
    }

    @Test
    fun `when first RSS is hidden then first edit title will be second RSS`() {
        // Default hidden option is enaled
        presenter.create()
        presenter.resume()
        presenter.onEditFeedMenuClicked(FIRST_RSS_POSITION)
        assertThat(view.editTitle, `is`(SECOND_RSS_TITLE))
    }

    @Test
    fun `when RSS is not hidden then first edit title will be first RSS`() {
        presenter.create()
        presenter.resume()
        // Disale hidden option
        presenter.onFeedListClicked(HIDE_OPTION_POSITION_WHEN_HIDDEN, object : RssListFragment.OnFeedListFragmentListener {
            override fun onListClicked(feedId: Int) {}

            override fun onAllUnreadClicked() {}
        })
        presenter.onEditFeedMenuClicked(FIRST_RSS_POSITION)
        assertThat(view.editTitle, `is`(FIRST_RSS_TITLE))
    }

    @Test
    fun `when first RSS is hidden then first RSS title will be second RSS`() {
        presenter.resume()
        assertThat(presenter.feeds.size, `is`(2)) // including hide line
        assertThat(presenter.feeds[0].title, `is`(SECOND_RSS_TITLE))
    }

    @Test
    fun `when all of articles were read then show all of RSS`() {
        val firstRss = Feed(FIRST_RSS_ID, FIRST_RSS_TITLE, "", "", "", 0, "")
        val secondRss = Feed(SECOND_RSS_ID, SECOND_RSS_TITLE, "", "", "", 0, "")
        val alreadyReadRss = arrayListOf(firstRss, secondRss)
        `when`(adapter.allFeedsWithNumOfUnreadArticles).thenReturn(alreadyReadRss)
        presenter.resume()
        assertThat(presenter.feeds.size, `is`(3)) // including hide line
        assertThat(presenter.feeds[0].title, `is`(FIRST_RSS_TITLE))
        assertThat(presenter.feeds[1].title, `is`(SECOND_RSS_TITLE))
    }

    private class MockView : RssListView {

        var editTitle: String? = null

        override fun showDeleteFeedAlertDialog(position: Int) {

        }

        override fun showEditTitleDialog(position: Int, feedTitle: String) {
            this.editTitle = feedTitle
        }

        override fun setRefreshing(doScroll: Boolean) {

        }

        override fun init(feeds: ArrayList<Feed>) {

        }

        override fun setTotalUnreadCount(count: Int) {

        }

        override fun onRefreshCompleted() {

        }

        override fun showEditFeedTitleEmptyErrorToast() {

        }

        override fun showEditFeedFailToast() {

        }

        override fun showEditFeedSuccessToast() {

        }

        override fun showDeleteSuccessToast() {

        }

        override fun showDeleteFailToast() {

        }

        override fun showAddFeedSuccessToast() {

        }

        override fun showGenericAddFeedErrorToast() {

        }

        override fun showInvalidUrlAddFeedErrorToast() {

        }

        override fun notifyDataSetChanged() {

        }

        override fun showAllUnreadView() {

        }

        override fun hideAllUnreadView() {

        }
    }

    companion object {

        private const val FIRST_RSS_TITLE = "rss1"
        private const val SECOND_RSS_TITLE = "rss2"
        private const val FIRST_RSS_ID = 0
        private const val SECOND_RSS_ID = 1
        private const val FIRST_RSS_POSITION = 0
        private const val HIDE_OPTION_POSITION_WHEN_HIDDEN = 1
    }
}