package com.phicdy.mycuration.presentation.presenter

import com.nhaarman.mockitokotlin2.mock
import com.phicdy.mycuration.domain.entity.Feed
import com.phicdy.mycuration.presentation.view.RegisterFilterView
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.ArrayList

class RegisterFilterPresenterTest {

    private lateinit var presenter: RegisterFilterPresenter
    private lateinit var mockView: MockView

    @Before
    fun setup() {
        mockView = MockView()
        presenter = RegisterFilterPresenter(mockView, mock(), 1)
    }

    @Test
    fun `when set one selected feed list then filter target is the one`() {
        val testFeeds = ArrayList<Feed>()
        testFeeds.add(Feed(1, "testFeed", "", Feed.DEDAULT_ICON_PATH, "", 0, ""))
        presenter.setSelectedFeedList(testFeeds)
        assertThat(mockView.filterTarget).isEqualTo("testFeed")
    }

    @Test
    fun `when set two selected feed list then filter target is multiple target`() {
        val testFeeds = ArrayList<Feed>()
        testFeeds.add(Feed(1, "testFeed", "", Feed.DEDAULT_ICON_PATH, "", 0, ""))
        testFeeds.add(Feed(2, "testFeed2", "", Feed.DEDAULT_ICON_PATH, "", 0, ""))
        presenter.setSelectedFeedList(testFeeds)
        assertThat(mockView.filterTarget).isEqualTo(MockView.MULTIPLE_FILTER_TARGET)
    }

    @Test
    fun `when set empty selected feed list then filter target is default`() {
        val testFeeds = ArrayList<Feed>()
        presenter.setSelectedFeedList(testFeeds)
        assertThat(mockView.filterTarget).isEqualTo(MockView.DEFAULT_FILTER_TARGET)
    }

    private class MockView : RegisterFilterView {
        private var title: String? = null
        var filterTarget = DEFAULT_FILTER_TARGET

        override fun filterKeyword(): String {
            return ""
        }

        override fun filterUrl(): String {
            return ""
        }

        override fun filterTitle(): String {
            return ""
        }

        override fun setFilterTitle(title: String) {
            this.title = title
        }

        override fun setFilterTargetRss(rss: String) {
            this.filterTarget = rss
        }

        override fun setMultipleFilterTargetRss() {
            this.filterTarget = MULTIPLE_FILTER_TARGET
        }

        override fun resetFilterTargetRss() {
            this.filterTarget = DEFAULT_FILTER_TARGET
        }

        override fun setFilterUrl(url: String) {

        }

        override fun setFilterKeyword(keyword: String) {

        }

        override fun handleEmptyTitle() {

        }

        override fun handleEmptyCondition() {

        }

        override fun handlePercentOnly() {

        }

        override fun finish() {

        }

        override fun showSaveSuccessToast() {

        }

        override fun showSaveErrorToast() {

        }

        override fun trackEdit() {

        }

        override fun trackRegister() {

        }

        companion object {
            const val DEFAULT_FILTER_TARGET = "default"
            const val MULTIPLE_FILTER_TARGET = "multiple-target"
        }
    }
}