package com.phicdy.mycuration.articlelist

import com.phicdy.action.articlelist.ReadAllArticlesAction
import com.phicdy.mycuration.articlelist.action.FetchArticleAction
import com.phicdy.mycuration.articlelist.action.ReadArticlePositionAction
import com.phicdy.mycuration.articlelist.action.ScrollAction
import com.phicdy.mycuration.articlelist.action.SwipeAction
import com.phicdy.mycuration.articlelist.ui.ArticleListUiState
import com.phicdy.mycuration.articlelist.ui.ArticleRowUi
import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.entity.FavoritableArticle
import com.phicdy.mycuration.entity.ReadAllArticles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
class ArticleListViewModelTest {

    private val dispatcher = Dispatcher()
    private lateinit var viewModel: ArticleListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ArticleListViewModel(scrollActionCreator = mock(), dispatcher = dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun article(id: Int) = FavoritableArticle(
        id = id,
        title = "title$id",
        url = "url$id",
        status = Article.UNREAD,
        point = "1",
        postedDate = 0,
        feedId = 1,
        feedTitle = "feed",
        feedIconPath = "icon",
        isFavorite = false
    )

    private fun readFlags(): List<Boolean> =
        (viewModel.uiState.value as ArticleListUiState.Loaded).rows
            .filterIsInstance<ArticleRowUi.Content>()
            .map { it.isRead }

    @Test
    fun `initial state is loading`() {
        assertThat(viewModel.uiState.value).isEqualTo(ArticleListUiState.Loading)
    }

    @Test
    fun `when articles are fetched then rows are emitted`() = runTest {
        dispatcher.dispatch(FetchArticleAction(listOf(ArticleItem.Content(article(1)), ArticleItem.Advertisement)))
        val state = viewModel.uiState.value as ArticleListUiState.Loaded
        assertThat(state.rows).hasSize(2)
        assertThat(state.rows[1]).isInstanceOf(ArticleRowUi.Advertisement::class.java)
    }

    @Test
    fun `when empty list is fetched then no article state is emitted`() = runTest {
        dispatcher.dispatch(FetchArticleAction(emptyList()))
        assertThat(viewModel.uiState.value)
            .isEqualTo(ArticleListUiState.Empty(ArticleListUiState.EmptyReason.NO_ARTICLE))
    }

    @Test
    fun `when article is read in place then new state is emitted`() =
        assertReSnapshotAfter { ReadArticlePositionAction(0) }

    @Test
    fun `when article is swiped in place then new state is emitted`() =
        assertReSnapshotAfter { SwipeAction(0) }

    @Test
    fun `when articles are read by scroll in place then new state is emitted`() =
        assertReSnapshotAfter { ScrollAction(1) }

    @Test
    fun `when all articles are read in place then new state is emitted`() =
        assertReSnapshotAfter { ReadAllArticlesAction(ReadAllArticles(1)) }

    private fun assertReSnapshotAfter(action: () -> Action<*>) = runTest {
        val first = article(1)
        val second = article(2)
        dispatcher.dispatch(FetchArticleAction(listOf(ArticleItem.Content(first), ArticleItem.Content(second))))
        val before = viewModel.uiState.value
        assertThat(readFlags()).containsExactly(false, false)

        // Simulate action creators which mutate the status in place
        first.status = Article.READ
        dispatcher.dispatch(action())

        assertThat(viewModel.uiState.value).isNotSameAs(before)
        assertThat(readFlags()).containsExactly(true, false)
    }
}
