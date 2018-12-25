package com.phicdy.mycuration.uitest


import androidx.test.InstrumentationRegistry
import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import android.view.View
import android.view.ViewGroup
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.rss.Article
import com.phicdy.mycuration.deleteAll
import com.phicdy.mycuration.presentation.view.activity.TopActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchArticleTest : UiTest() {

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
        val helper = DatabaseHelper(getTargetContext())
        articleRepository = ArticleRepository(helper.writableDatabase)
        rssRepository = RssRepository(helper.writableDatabase, articleRepository, FilterRepository(helper.writableDatabase))
        deleteAll(helper.writableDatabase)
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
    fun noResultShowsWhenNoArticlesFound() {
        addTestRss()
        openSearchResult("b")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        assertNotNull(device.wait<UiObject2>(Until.findObject(By.text("該当する記事はありません")), 5000))
    }

    @Test
    fun resultShowsWhenArticleFound() {
        addTestRss()
        openSearchResult(testArticleTitle)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        assertNull(device.wait<UiObject2>(Until.findObject(By.text("該当する記事はありません")), 5000))
        val textView = onView(
                allOf(withId(R.id.articleTitle), withText(testArticleTitle),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java),
                                        0),
                                0),
                        isDisplayed()))
        textView.check(matches(withText(testArticleTitle)))

        val textView2 = onView(
                allOf(withId(R.id.feedTitle), withText(testRssTitle),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java),
                                        1),
                                1),
                        isDisplayed()))
        textView2.check(matches(withText(testRssTitle)))

        val textView3 = onView(
                allOf(withId(R.id.tv_articleUrl), withText(testArticleUrl),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java),
                                        0),
                                2),
                        isDisplayed()))
        textView3.check(matches(withText(testArticleUrl)))

        val textView4 = onView(
                allOf(withId(R.id.articlePostedTime), withText(testArticleDateStr),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java),
                                        3),
                                0),
                        isDisplayed()))
        textView4.check(matches(withText(testArticleDateStr)))

        val textView5 = onView(
                allOf(withId(R.id.articlePoint), withText(testArticlePoint),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java),
                                        3),
                                2),
                        isDisplayed()))
        textView5.check(matches(withText(testArticlePoint)))

        val recyclerView = onView(
                allOf(withId(R.id.rv_article),
                        childAtPosition(
                                allOf(withId(R.id.fr_article_search_result),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.view.ViewGroup::class.java),
                                                1)),
                                0),
                        isDisplayed()))
        recyclerView.check(matches(isDisplayed()))
    }

    private fun openSearchResult(query: String) {
        val actionMenuItemView = onView(
                allOf(withId(R.id.search_article_top_activity), withContentDescription("記事を検索"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar_top),
                                        1),
                                0),
                        isDisplayed()))
        actionMenuItemView.perform(click())

        val searchAutoComplete = onView(
                allOf(withId(R.id.search_src_text),
                        childAtPosition(
                                allOf(withId(R.id.search_plate),
                                        childAtPosition(
                                                withId(R.id.search_edit_frame),
                                                1)),
                                0),
                        isDisplayed()))
        searchAutoComplete.perform(replaceText(query), closeSoftKeyboard())
        val searchAutoComplete2 = onView(
                allOf(withId(R.id.search_src_text), withText(query),
                        childAtPosition(
                                allOf(withId(R.id.search_plate),
                                        childAtPosition(
                                                withId(R.id.search_edit_frame),
                                                1)),
                                0),
                        isDisplayed()))
        searchAutoComplete2.perform(pressImeActionButton())
    }

    private fun addTestRss() = runBlocking {
        val feed = rssRepository.store(testRssTitle, testRssUrl, "RSS1.0", "http://hoge,com")
        assertNotNull(feed)
        // postDate: 2018-01-01 12:34:56
        val articles = arrayListOf(Article(1, testArticleTitle, testArticleUrl, Article.UNREAD,
                testArticlePoint, testArticleDateLong, feed!!.id, feed.title, ""))
        articleRepository.saveNewArticles(articles, feed.id)
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

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
