package com.phicdy.mycuration.uitest


import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.phicdy.mycuration.CoroutineTestRule
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.deleteAll
import com.phicdy.mycuration.entity.Article
import com.phicdy.mycuration.repository.Database
import com.phicdy.mycuration.top.TopActivity
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.*
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.runner.RunWith

@Ignore("Not passed. Ignore until fixed")
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SearchArticleTest : UiTest() {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    companion object {
        private const val testRssTitle = "testRss"
        private const val testRssUrl = "http://hoge.com"
        private const val testArticleTitle = "testArtilce"
        private const val testArticleDateStr = "2018/01/01 12:34:56"
        private const val testArticleDateLong = 1514777696000L
        private const val testArticleUrl = "http://hoge.com/a"
        private const val testArticlePoint = "1"
    }

    @JvmField
    @Rule
    var mActivityTestRule = ActivityTestRule(TopActivity::class.java)

    private lateinit var articleRepository: ArticleRepository
    private lateinit var rssRepository: RssRepository

    @Before
    fun setup() {
        val db = Database(
            AndroidSqliteDriver(
                schema = Database.Schema,
                context = ApplicationProvider.getApplicationContext(),
                name = "rss_manage"
            )
        )

        articleRepository = ArticleRepository(db, coroutineTestRule.testCoroutineDispatcherProvider, coroutineTestRule.testCoroutineScope)
        rssRepository = RssRepository(db, articleRepository, FilterRepository(db, coroutineTestRule.testCoroutineDispatcherProvider), coroutineTestRule.testCoroutineScope, coroutineTestRule.testCoroutineDispatcherProvider)
        deleteAll(db)
        super.setup(mActivityTestRule.activity)
    }

    @After
    public override fun tearDown() {
        super.tearDown()
    }

    @Test
    fun noResultShowsWhenNoArticlesExist() {
        openSearchResult("a")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        assertNotNull(device.wait<UiObject2>(Until.findObject(By.text("該当する記事はありません")), 5000))
    }

    @Test
    fun noResultShowsWhenNoArticlesFound() = coroutineTestRule.testCoroutineScope.runTest {
        addTestRss()
        openSearchResult("b")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        assertNotNull(device.wait<UiObject2>(Until.findObject(By.text("該当する記事はありません")), 5000))
    }

    @Test
    fun resultShowsWhenArticleFound() = coroutineTestRule.testCoroutineScope.runTest {
        addTestRss()
        openSearchResult(testArticleTitle)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        assertNull(device.wait<UiObject2>(Until.findObject(By.text("該当する記事はありません")), 5000))

        val title = onView(
                allOf(
                        withId(R.id.articleTitle), withText(testArticleTitle),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.rv_article),
                                        0
                                ),
                                0
                        ),
                        isDisplayed()
                )
        )
        title.check(matches(withText(testArticleTitle)))

        val rss = onView(
            allOf(
                withId(R.id.feedTitle), withText(testRssTitle),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.rv_article),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        rss.check(matches(withText(testRssTitle)))

        val textView3 = onView(
            allOf(
                withId(R.id.tv_articleUrl), withText(testArticleUrl),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.rv_article),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        textView3.check(matches(withText(testArticleUrl)))

        val date = onView(
            allOf(
                withId(R.id.articlePostedTime), withText(testArticleDateStr),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.rv_article),
                        0
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        date.check(matches(withText(testArticleDateStr)))

        val point = onView(
            allOf(
                withId(R.id.articlePoint), withText(testArticlePoint),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.rv_article),
                        0
                    ),
                    6
                ),
                isDisplayed()
            )
        )
        point.check(matches(withText(testArticlePoint)))
    }

    private fun openSearchResult(query: String) {
        val actionMenuItemView = onView(
            allOf(
                withId(R.id.search_article_top_activity), withContentDescription("記事を検索"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar_top),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView.perform(click())

        val searchAutoComplete = onView(
            allOf(
                withId(R.id.search_src_text),
                childAtPosition(
                    allOf(
                        withId(R.id.search_plate),
                        childAtPosition(
                            withId(R.id.search_edit_frame),
                            1
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        searchAutoComplete.perform(replaceText(query), closeSoftKeyboard())
        val searchAutoComplete2 = onView(
            allOf(
                withId(R.id.search_src_text), withText(query),
                childAtPosition(
                    allOf(
                        withId(R.id.search_plate),
                        childAtPosition(
                            withId(R.id.search_edit_frame),
                            1
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        searchAutoComplete2.perform(pressImeActionButton())
    }

    private suspend fun addTestRss() {
        val feed = rssRepository.store(testRssTitle, testRssUrl, "RSS1.0", "http://hoge,com")
        assertNotNull(feed)
        // postDate: 2018-01-01 12:34:56
        val articles = arrayListOf(
                Article(
                        1, testArticleTitle, testArticleUrl, Article.UNREAD,
                        testArticlePoint, testArticleDateLong, feed!!.id, feed.title, ""
                )
        )
        articleRepository.saveNewArticles(articles, feed.id)
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return (parent is ViewGroup && parentMatcher.matches(parent)
                    && view == parent.getChildAt(position))
            }
        }
    }
}
