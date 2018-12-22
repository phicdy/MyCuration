package com.phicdy.mycuration.presentation.presenter


import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.data.rss.Feed
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.view.RssItemView
import com.phicdy.mycuration.presentation.view.RssListView
import com.phicdy.mycuration.presentation.view.fragment.RssListFragment
import com.phicdy.mycuration.util.PreferenceHelper
import io.reactivex.Flowable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
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

        presenter = RssListPresenter(view, PreferenceHelper, mockRssRepository, networkTaskManager,
                UnreadCountRepository(mockRssRepository, mock()))

        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    // For coverage
    @Test
    fun testOnCreate() {
        presenter.create()
    }

    @Test
    fun `when onResume and RSS doesn't exist then hide all unread view`() = runBlocking {
        whenever(mockRssRepository.getNumOfRss()).thenReturn(0)
        presenter.resume()
        verify(view, times(1)).hideAllUnreadView()
    }

    @Test
    fun `when onResume and RSS doesn't exist then hide recyclerview`() = runBlocking {
        whenever(mockRssRepository.getNumOfRss()).thenReturn(0)
        presenter.resume()
        verify(view, times(1)).hideRecyclerView()
    }

    @Test
    fun `when onResume and RSS doesn't exist then show empty view`() = runBlocking {
        whenever(mockRssRepository.getNumOfRss()).thenReturn(0)
        presenter.resume()
        verify(view, times(1)).showEmptyView()
    }

    @Test
    fun `when onResume and RSS exist then show all unread view`() = runBlocking {
        presenter.resume()
        verify(view, times(1)).showAllUnreadView()
    }

    @Test
    fun `when onResume and RSS exist then show recyclerview`() = runBlocking {
        presenter.resume()
        verify(view, times(1)).showRecyclerView()
    }

    @Test
    fun `when onResume and RSS exist then hide empty view`() = runBlocking {
        presenter.resume()
        verify(view, times(1)).hideEmptyView()
    }

    @Test
    fun `when onResume and RSS exist then set num of unread count`() = runBlocking {
        presenter.resume()
        verify(view, times(1)).setTotalUnreadCount(1)
    }

    @Test
    fun `when onResume and RSS exist then fetch RSS from database`() {
        runBlocking {
            presenter.resume()
            // Also called in UnreadCountRepository
            verify(mockRssRepository, times(2)).getAllFeedsWithNumOfUnreadArticles()
        }
    }

    @Test
    fun `when onResume and RSS exist then init with hidden list`() = runBlocking {
        presenter.resume()
        verify(view, times(1)).init(presenter.unreadOnlyFeeds)
    }

    @Test
    fun `when onResume and show all RSS and onResume then init with all list`() = runBlocking {
        presenter.resume()
        presenter.onRssFooterClicked() // call init(allFeeds)
        presenter.pause()
        presenter.resume()
        verify(view, times(2)).init(presenter.allFeeds)
    }

    @Test
    fun `when onResume and RSS exist and auto update in main UI is enabled and after interval then show refreshing view`() = runBlocking {
        whenever(mockPref.getBoolean(anyString(), any())).thenReturn(true)
        whenever(mockPref.getLong(anyString(), anyLong())).thenReturn(System.currentTimeMillis()-1000*60)
        whenever(networkTaskManager.updateAllFeeds(allFeeds)).thenReturn(Flowable.just(mock()))
        presenter.resume()
        verify(view, times(1)).setRefreshing(true)
    }

    @Test
    fun `when onResume and RSS exist and auto update in main UI is enabled and before interval then show refreshing view`() = runBlocking {
        whenever(mockPref.getBoolean(anyString(), any())).thenReturn(true)
        whenever(mockPref.getLong(anyString(), anyLong())).thenReturn(System.currentTimeMillis())
        presenter.resume()
        verify(view, times(0)).setRefreshing(true)
    }

    @Test
    fun `when onResume and RSS exist and auto update in main UI is disabled then not show refreshing view`() = runBlocking {
        whenever(mockPref.getBoolean(anyString(), any())).thenReturn(false)
        presenter.resume()
        verify(view, times(0)).setRefreshing(true)
    }

    @Test
    fun `when first RSS is hidden then first RSS title will be second RSS`() = runBlocking {
        presenter.resume()
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
        presenter.resume()
        assertThat(presenter.unreadOnlyFeeds)
                .hasSize(2)
                .extracting("title")
                .contains(FIRST_RSS_TITLE, SECOND_RSS_TITLE)
        return@runBlocking
    }

    @Test
    fun `when delete menu is clicked then show alert dialog`() {
        presenter.onDeleteFeedMenuClicked(0)
        verify(view, times(1)).showDeleteFeedAlertDialog(0)
    }

    @Test
    fun `when first RSS is hidden then first edit title will be second RSS`() = runBlocking {
        // Default hidden option is enaled
        presenter.resume()
        presenter.onEditFeedMenuClicked(FIRST_RSS_POSITION)
        verify(view, times(1)).showEditTitleDialog(FIRST_RSS_POSITION, SECOND_RSS_TITLE)
    }

    @Test
    fun `when RSS is not hidden then first edit title will be first RSS`() = runBlocking {
        presenter.resume()
        presenter.onRssFooterClicked()
        presenter.onEditFeedMenuClicked(FIRST_RSS_POSITION)
        verify(view, times(1)).showEditTitleDialog(FIRST_RSS_POSITION, FIRST_RSS_TITLE)
    }

    @Test
    fun `when edit ok button is clicked and new title is empty then show error toast`() = runBlocking {
        presenter.onEditFeedOkButtonClicked("", 0)
        verify(view, times(1)).showEditFeedTitleEmptyErrorToast()
    }

    @Test
    fun `when edit ok button is clicked and new title is blank then show error toast`() = runBlocking {
        presenter.onEditFeedOkButtonClicked("   ", 0)
        verify(view, times(1)).showEditFeedTitleEmptyErrorToast()
    }

    @Test
    fun `when edit ok button is clicked and succeeds then show success toast`() = runBlocking {
        whenever(mockRssRepository.saveNewTitle(anyInt(), anyString())).thenReturn(1)
        presenter.onEditFeedOkButtonClicked("newTitle", 0)
        verify(view, times(1)).showEditFeedSuccessToast()
    }

    @Test
    fun `when edit ok button is clicked and succeeds then refresh the list`() = runBlocking {
        whenever(mockRssRepository.saveNewTitle(anyInt(), anyString())).thenReturn(1)
        presenter.onEditFeedOkButtonClicked("newTitle", 0)
        verify(view, times(1)).notifyDataSetChanged()
    }

    @Test
    fun `when edit ok button is clicked and succeeds then the title will be updated`() = runBlocking {
        whenever(mockRssRepository.saveNewTitle(anyInt(), anyString())).thenReturn(1)
        presenter.resume() // init list
        presenter.onEditFeedOkButtonClicked("newTitle", 0)
        // Current status is hidden, first position RSS is first one in hidden RSS list and second one in all RSS list
        assertThat(presenter.unreadOnlyFeeds)
                .hasSize(1)
                .extracting("title")
                .contains("newTitle")
        assertThat(presenter.allFeeds)
                .hasSize(2)
                .extracting("title")
                .containsExactly(FIRST_RSS_TITLE, "newTitle")
        return@runBlocking
    }

    @Test
    fun `when edit ok button is clicked and fails then show error toast`() = runBlocking {
        whenever(mockRssRepository.saveNewTitle(anyInt(), anyString())).thenReturn(0)
        presenter.onEditFeedOkButtonClicked("newTitle", 0)
        verify(view, times(1)).showEditFeedFailToast()
    }

    @Test
    fun `when delete ok button is clicked and fails then show error toast`() = runBlocking {
        whenever(mockRssRepository.deleteRss(anyInt())).thenReturn(false)
        presenter.onDeleteOkButtonClicked(0)
        verify(view, times(1)).showDeleteFailToast()
    }

    @Test
    fun `when delete ok button is clicked and succeeds then show success toast`() = runBlocking {
        whenever(mockRssRepository.deleteRss(anyInt())).thenReturn(true)
        presenter.resume() // init list
        presenter.onDeleteOkButtonClicked(0)
        verify(view, times(1)).showDeleteSuccessToast()
    }

    @Test
    fun `when delete ok button is clicked in hidden status and succeeds then delete the RSS`() = runBlocking {
        whenever(mockRssRepository.deleteRss(anyInt())).thenReturn(true)
        presenter.resume() // init list
        presenter.onDeleteOkButtonClicked(0)
        // Current status is hidden and size is 1, so hidden list becomes all RSS list after refresh
        assertThat(presenter.unreadOnlyFeeds)
                .hasSize(1)
                .extracting("id")
                .contains(FIRST_RSS_ID)
        assertThat(presenter.allFeeds)
                .hasSize(1)
                .extracting("id")
                .contains(FIRST_RSS_ID)
        return@runBlocking
    }

    @Test
    fun `when delete ok button is clicked in all of RSS and succeeds then delete the RSS`() = runBlocking {
        whenever(mockRssRepository.deleteRss(anyInt())).thenReturn(true)
        presenter.resume() // init list
        presenter.onRssFooterClicked() // Change to all RSS
        presenter.onDeleteOkButtonClicked(0)
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
        presenter.resume() // init list
        presenter.onRssFooterClicked() // Change to all RSS
        presenter.onDeleteOkButtonClicked(0)
        presenter.onDeleteOkButtonClicked(0)
        verify(view, times(1)).showEmptyView()
    }

    @Test
    fun `when RSS is clicked then callback is called`() = runBlocking {
        val listner = mock<RssListFragment.OnFeedListFragmentListener>()
        presenter.resume() // init list
        presenter.onRssItemClicked(0, listner)
        verify(listner, times(1)).onListClicked(SECOND_RSS_ID)
    }

    @Test
    fun `when invalid RSS is clicked then callback is not called`() = runBlocking {
        val listner = mock<RssListFragment.OnFeedListFragmentListener>()
        presenter.resume() // init list
        presenter.onRssItemClicked(9999, listner)
        verify(listner, times(0)).onListClicked(SECOND_RSS_ID)
    }

    @Test
    fun `when RSS is clicked and listener is null then not crashed`() = runBlocking {
        presenter.resume() // init list
        presenter.onRssItemClicked(0, null)
    }

    @Test
    fun `when refresh and RSS is empty then finish refresh`() = runBlocking {
        whenever(mockRssRepository.getAllFeedsWithNumOfUnreadArticles()).thenReturn(arrayListOf())
        presenter.resume()
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
            // Also called in UnreadCountRepository
            verify(mockRssRepository, times(2)).getAllFeedsWithNumOfUnreadArticles()
        }
    }

    @Test
    fun `when finish refresh then reload RSS list`() {
        runBlocking { presenter.onFinishUpdate() }
        verify(view, times(1)).init(presenter.unreadOnlyFeeds)
    }

    @Test
    fun `when finish refresh then last update time will be updated`() {
        runBlocking { presenter.onFinishUpdate() }
        verify(mockPref.edit(), times(1)).putLong(anyString(), anyLong())
    }

    @Test
    fun `when get item count in RecyclerView in hide status then return num of unread RSS + 1 for footer`() = runBlocking {
        presenter.resume()
        assertThat(presenter.getItemCount()).isEqualTo(2)
        return@runBlocking
    }

    @Test
    fun `when get item count in RecyclerView in all status then return num of unread RSS + 1 for footer`() = runBlocking {
        presenter.resume()
        presenter.onRssFooterClicked()
        assertThat(presenter.getItemCount()).isEqualTo(3)
        return@runBlocking
    }

    @Test
    fun `when bind default icon RSS then show default icon`() = runBlocking {
        presenter.resume()
        presenter.onRssFooterClicked()
        val mockRssItemView = mock<RssItemView.Content>()
        presenter.onBindRssViewHolder(0, mockRssItemView)
        verify(mockRssItemView, times(1)).showDefaultIcon()
    }

    @Test
    fun `when bind default icon RSS and fails to show the icon then show default icon`() = runBlocking {
        presenter.resume()
        presenter.onRssFooterClicked()
        val mockRssItemView = mock<RssItemView.Content>()
        presenter.onBindRssViewHolder(0, mockRssItemView)
        verify(mockRssItemView, times(1)).showDefaultIcon()

    }

    @Test
    fun `when bind RSS then update the title`() = runBlocking {
        presenter.resume()
        presenter.onRssFooterClicked()
        val mockRssItemView = mock<RssItemView.Content>()
        presenter.onBindRssViewHolder(0, mockRssItemView)
        verify(mockRssItemView, times(1)).updateTitle(FIRST_RSS_TITLE)
    }

    @Test
    fun `when bind RSS in all status then update unread count`() = runBlocking {
        presenter.resume()
        presenter.onRssFooterClicked()
        val mockRssItemView = mock<RssItemView.Content>()
        presenter.onBindRssViewHolder(0, mockRssItemView)
        verify(mockRssItemView, times(1)).updateUnreadCount(FIRST_RSS_UNREAD_COUNT.toString())
    }

    @Test
    fun `when bind RSS in hidden status then update unread count`() = runBlocking {
        presenter.resume()
        val mockRssItemView = mock<RssItemView.Content>()
        presenter.onBindRssViewHolder(0, mockRssItemView)
        verify(mockRssItemView, times(1)).updateUnreadCount(SECOND_RSS_UNREAD_COUNT.toString())
    }

    @Test
    fun `when bind footer in all status then show hide view`() = runBlocking {
        presenter.resume()
        presenter.onRssFooterClicked()
        val mockRssFooterView = mock<RssItemView.Footer>()
        presenter.onBindRssFooterViewHolder(mockRssFooterView)
        verify(mockRssFooterView, times(1)).showHideView()
    }

    @Test
    fun `when bind footer in hidden status then show all view`() = runBlocking {
        presenter.resume()
        val mockRssFooterView = mock<RssItemView.Footer>()
        presenter.onBindRssFooterViewHolder(mockRssFooterView)
        verify(mockRssFooterView, times(1)).showAllView()
    }

    @Test
    fun `when get item view type in hide status and position is same with size then rturn footer`() = runBlocking {
        presenter.resume()
        assertThat(presenter.onGetItemViewType(1)).isEqualTo(RssListFragment.VIEW_TYPE_FOOTER)
        return@runBlocking
    }

    @Test
    fun `when get item view type in hide status and position is not same with size then rturn footer`() = runBlocking {
        presenter.resume()
        assertThat(presenter.onGetItemViewType(0)).isEqualTo(RssListFragment.VIEW_TYPE_RSS)
        return@runBlocking
    }

    @Test
    fun `when get item view type in all status and position is same with size then rturn footer`() = runBlocking {
        presenter.resume()
        presenter.onRssFooterClicked()
        assertThat(presenter.onGetItemViewType(2)).isEqualTo(RssListFragment.VIEW_TYPE_FOOTER)
        return@runBlocking
    }

    @Test
    fun `when get item view type in all status and position is not same with size then rturn footer`() = runBlocking {
        presenter.resume()
        presenter.onRssFooterClicked()
        assertThat(presenter.onGetItemViewType(0)).isEqualTo(RssListFragment.VIEW_TYPE_RSS)
        return@runBlocking
    }

    @Test
    fun `when click footer twice then go back to hidden status`() = runBlocking {
        presenter.resume()
        presenter.onRssFooterClicked()
        presenter.onRssFooterClicked()
        assertThat(presenter.getItemCount()).isEqualTo(2)
        return@runBlocking
    }

    companion object {

        private const val FIRST_RSS_TITLE = "rss1"
        private const val SECOND_RSS_TITLE = "rss2"
        private const val FIRST_RSS_ID = 0
        private const val SECOND_RSS_ID = 1
        private const val FIRST_RSS_POSITION = 0
        private const val SECOND_RSS_ICON_PATH = "https://www.google.com/icon"
        private const val FIRST_RSS_UNREAD_COUNT = 0
        private const val SECOND_RSS_UNREAD_COUNT = 1
    }
}