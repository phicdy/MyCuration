package com.phicdy.mycuration.di

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.R
import com.phicdy.mycuration.admob.AdmobProvider
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.articlelist.ArticlesListFragment
import com.phicdy.mycuration.articlelist.FavoriteArticlesListFragment
import com.phicdy.mycuration.articlelist.action.FetchAllArticleListActionCreator
import com.phicdy.mycuration.articlelist.action.FetchArticleListOfRssActionCreator
import com.phicdy.mycuration.articlelist.action.FetchFavoriteArticleListActionCreator
import com.phicdy.mycuration.articlelist.action.SearchArticleListActionCreator
import com.phicdy.mycuration.articlelist.action.UpdateFavoriteStatusActionCreator
import com.phicdy.mycuration.articlelist.store.ArticleListStore
import com.phicdy.mycuration.articlelist.store.FinishStateStore
import com.phicdy.mycuration.articlelist.store.OpenExternalWebBrowserStateStore
import com.phicdy.mycuration.articlelist.store.OpenInternalWebBrowserStateStore
import com.phicdy.mycuration.articlelist.store.ReadAllArticlesStateStore
import com.phicdy.mycuration.articlelist.store.ReadArticlePositionStore
import com.phicdy.mycuration.articlelist.store.ScrollPositionStore
import com.phicdy.mycuration.articlelist.store.SearchResultStore
import com.phicdy.mycuration.articlelist.store.ShareUrlStore
import com.phicdy.mycuration.articlelist.store.SwipePositionStore
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.curatedarticlelist.CuratedArticlesListFragment
import com.phicdy.mycuration.curatedarticlelist.action.FetchCuratedArticleListActionCreator
import com.phicdy.mycuration.curatedarticlelist.store.CuratedArticleListStore
import com.phicdy.mycuration.curatedarticlelist.store.FinishCuratedArticleStateStore
import com.phicdy.mycuration.curatedarticlelist.store.OpenCuratedArticleWithExternalWebBrowserStateStore
import com.phicdy.mycuration.curatedarticlelist.store.OpenCuratedArticleWithInternalWebBrowserStateStore
import com.phicdy.mycuration.curatedarticlelist.store.ReadAllCuratedArticlesStateStore
import com.phicdy.mycuration.curatedarticlelist.store.ReadCuratedArticlePositionStore
import com.phicdy.mycuration.curatedarticlelist.store.ScrollCuratedArticlePositionStore
import com.phicdy.mycuration.curatedarticlelist.store.ShareCuratedArticleUrlStore
import com.phicdy.mycuration.curatedarticlelist.store.SwipeCuratedArticlePositionStore
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.AdditionalSettingApi
import com.phicdy.mycuration.data.repository.AdditionalSettingRepository
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.FavoriteRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.domain.rss.RssParseExecutor
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.presenter.AddCurationPresenter
import com.phicdy.mycuration.presentation.presenter.CurationListPresenter
import com.phicdy.mycuration.presentation.presenter.FeedSearchPresenter
import com.phicdy.mycuration.presentation.presenter.FeedUrlHookPresenter
import com.phicdy.mycuration.presentation.presenter.FilterListPresenter
import com.phicdy.mycuration.presentation.presenter.RegisterFilterPresenter
import com.phicdy.mycuration.presentation.presenter.SettingPresenter
import com.phicdy.mycuration.presentation.presenter.TopActivityPresenter
import com.phicdy.mycuration.presentation.view.AddCurationView
import com.phicdy.mycuration.presentation.view.CurationListView
import com.phicdy.mycuration.presentation.view.FeedSearchView
import com.phicdy.mycuration.presentation.view.FeedUrlHookView
import com.phicdy.mycuration.presentation.view.RegisterFilterView
import com.phicdy.mycuration.presentation.view.SettingView
import com.phicdy.mycuration.presentation.view.TopActivityView
import com.phicdy.mycuration.presentation.view.activity.FeedSearchActivity
import com.phicdy.mycuration.presentation.view.activity.FeedUrlHookActivity
import com.phicdy.mycuration.presentation.view.activity.RegisterFilterActivity
import com.phicdy.mycuration.presentation.view.activity.TopActivity
import com.phicdy.mycuration.presentation.view.fragment.AddCurationFragment
import com.phicdy.mycuration.presentation.view.fragment.CurationListFragment
import com.phicdy.mycuration.presentation.view.fragment.FilterListFragment
import com.phicdy.mycuration.presentation.view.fragment.SettingFragment
import com.phicdy.mycuration.rss.ChangeRssListModeActionCreator
import com.phicdy.mycuration.rss.ChangeRssTitleActionCreator
import com.phicdy.mycuration.rss.DeleteRssActionCreator
import com.phicdy.mycuration.rss.FetchAllRssListActionCreator
import com.phicdy.mycuration.rss.RSSListStateStore
import com.phicdy.mycuration.rss.RssListFragment
import com.phicdy.mycuration.rss.RssListItemFactory
import com.phicdy.mycuration.rss.UpdateAllRssActionCreator
import com.phicdy.mycuration.util.log.TimberTree
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val appModule = module {

    single<SQLiteDatabase> { DatabaseHelper(androidApplication()).writableDatabase }
    single { RssRepository(get(), get(), get()) }
    single { ArticleRepository(get()) }
    single { CurationRepository(get()) }
    single { FilterRepository(get()) }
    single { FavoriteRepository(get()) }
    single { PreferenceHelper }
    single { NetworkTaskManager(get(), get(), get(), get()) }
    single<AdditionalSettingApi> { AdditionalSettingRepository(get(), get()) }
    single { AlarmManagerTaskManager(androidContext()) }
    single { Dispatcher() }
    single { OkHttpClient() }
    single<AdProvider> { AdmobProvider() }

    single { TimberTree() }

    scope(named<TopActivity>()) {
        scoped { (view: TopActivityView) ->
            TopActivityPresenter(
                    view = view,
                    articleRepository = get(),
                    rssRepository = get(),
                    helper = get()
            )
        }
    }

    scope(named<RssListFragment>()) {
        scoped {
            FetchAllRssListActionCreator(
                    dispatcher = get(),
                    rssRepository = get(),
                    rssListItemFactory = RssListItemFactory()
            )
        }
        scoped {
            UpdateAllRssActionCreator(
                    dispatcher = get(),
                    networkTaskManager = get(),
                    preferenceHelper = get(),
                    rssRepository = get()
            )
        }
        scoped {
            ChangeRssListModeActionCreator(
                    dispatcher = get(),
                    rssListItemFactory = RssListItemFactory()
            )
        }
        scoped {
            ChangeRssTitleActionCreator(
                    dispatcher = get(),
                    rssListItemFactory = RssListItemFactory()
            )
        }
        scoped {
            DeleteRssActionCreator(
                    dispatcher = get(),
                    rssListItemFactory = RssListItemFactory()
            )
        }
        viewModel { RSSListStateStore(get(), RssListItemFactory()) }
    }

    scope(named<ArticlesListFragment>()) {
        scoped { (rssId: Int) ->
            FetchArticleListOfRssActionCreator(
                    dispatcher = get(),
                    articleRepository = get(),
                    preferenceHelper = get(),
                    rssId = rssId
            )
        }
        scoped {
            FetchAllArticleListActionCreator(
                    dispatcher = get(),
                    articleRepository = get(),
                    preferenceHelper = get()
            )
        }
        scoped { (query: String) ->
            SearchArticleListActionCreator(
                    dispatcher = get(),
                    articleRepository = get(),
                    preferenceHelper = get(),
                    query = query
            )
        }
        scoped {
            UpdateFavoriteStatusActionCreator(
                    dispatcher = get(),
                    favoriteRepository = get()
            )
        }
        viewModel { ArticleListStore(get()) }
        viewModel { SearchResultStore(get()) }
        viewModel { FinishStateStore(get()) }
        viewModel { ReadArticlePositionStore(get()) }
        viewModel { OpenInternalWebBrowserStateStore(get()) }
        viewModel { OpenExternalWebBrowserStateStore(get()) }
        viewModel { ScrollPositionStore(get()) }
        viewModel { SwipePositionStore(get()) }
        viewModel { ReadAllArticlesStateStore(get()) }
        viewModel { ShareUrlStore(get()) }
    }

    scope(named<FavoriteArticlesListFragment>()) {
        scoped {
            FetchFavoriteArticleListActionCreator(
                    dispatcher = get(),
                    favoriteRepository = get(),
                    preferenceHelper = get()
            )
        }
        scoped {
            UpdateFavoriteStatusActionCreator(
                    dispatcher = get(),
                    favoriteRepository = get()
            )
        }
        viewModel { ArticleListStore(get()) }
        viewModel { FinishStateStore(get()) }
        viewModel { ReadArticlePositionStore(get()) }
        viewModel { OpenInternalWebBrowserStateStore(get()) }
        viewModel { OpenExternalWebBrowserStateStore(get()) }
        viewModel { ScrollPositionStore(get()) }
        viewModel { SwipePositionStore(get()) }
        viewModel { ReadAllArticlesStateStore(get()) }
        viewModel { ShareUrlStore(get()) }
    }

    scope(named<CuratedArticlesListFragment>()) {
        scoped { (curationId: Int) ->
            FetchCuratedArticleListActionCreator(
                    dispatcher = get(),
                    articleRepository = get(),
                    preferenceHelper = get(),
                    curationId = curationId
            )
        }
        viewModel { CuratedArticleListStore(get()) }
        viewModel { FinishCuratedArticleStateStore(get()) }
        viewModel { ReadCuratedArticlePositionStore(get()) }
        viewModel { OpenCuratedArticleWithInternalWebBrowserStateStore(get()) }
        viewModel { OpenCuratedArticleWithExternalWebBrowserStateStore(get()) }
        viewModel { ScrollCuratedArticlePositionStore(get()) }
        viewModel { SwipeCuratedArticlePositionStore(get()) }
        viewModel { ReadAllCuratedArticlesStateStore(get()) }
        viewModel { ShareCuratedArticleUrlStore(get()) }
    }

    scope(named<CurationListFragment>()) {
        scoped { (view: CurationListView) ->
            CurationListPresenter(
                    view = view,
                    rssRepository = get(),
                    curationRepository = get()
            )
        }
    }

    scope(named<FeedSearchActivity>()) {
        scoped { (view: FeedSearchView, coroutineScope: CoroutineScope) ->
            FeedSearchPresenter(
                    view = view,
                    rssRepository = get(),
                    networkTaskManager = get(),
                    coroutineScope = coroutineScope,
                    executor = RssParseExecutor(RssParser(), get())
            )
        }
    }

    scope(named<FeedUrlHookActivity>()) {
        scoped { (view: FeedUrlHookView, action: String, dataString: String, extrasText: String, coroutineScoe: CoroutineScope) ->
            FeedUrlHookPresenter(
                    view = view,
                    rssRepository = get(),
                    networkTaskManager = get(),
                    action = action,
                    dataString = dataString,
                    extrasText = extrasText,
                    coroutineScope = coroutineScoe,
                    parser = RssParser()
            )
        }
    }

    scope(named<RegisterFilterActivity>()) {
        scoped { (view: RegisterFilterView, editFilterId: Int) ->
            RegisterFilterPresenter(
                    view = view,
                    filterRepository = get(),
                    editFilterId = editFilterId
            )
        }
    }

    scope(named<FilterListFragment>()) {
        scoped { (view: FilterListFragment) ->
            FilterListPresenter(
                    view = view,
                    rssRepository = get(),
                    filterRepository = get()
            )
        }
    }

    scope(named<AddCurationFragment>()) {
        scoped { (view: AddCurationView) ->
            AddCurationPresenter(
                    view = view,
                    repository = get()
            )
        }
    }

    scope(named<SettingFragment>()) {
        scoped { (view: SettingView) ->
            val updateIntervalHourItems = get<Context>().resources.getStringArray(R.array.update_interval_items_values)
            val updateIntervalStringItems = get<Context>().resources.getStringArray(R.array.update_interval_items)
            val themeItems = get<Context>().resources.getStringArray(R.array.theme_items_values)
            val themeStringItems = get<Context>().resources.getStringArray(R.array.theme_items)
            val allReadBehaviorItems = get<Context>().resources.getStringArray(R.array.all_read_behavior_values)
            val allReadBehaviorStringItems = get<Context>().resources.getStringArray(R.array.all_read_behavior)
            val launchTabItems = get<Context>().resources.getStringArray(R.array.launch_tab_items_values)
            val launchTabStringItems = get<Context>().resources.getStringArray(R.array.launch_tab_items)
            val swipeDirectionItems = get<Context>().resources.getStringArray(R.array.swipe_direction_items_values)
            val swipeDirectionStringItems = get<Context>().resources.getStringArray(R.array.swipe_direction_items)
            SettingPresenter(
                    view = view,
                    helper = get(),
                    addtionalSettingApi = get(),
                    updateIntervalHourItems = updateIntervalHourItems,
                    updateIntervalStringItems = updateIntervalStringItems,
                    themeItems = themeItems,
                    themeStringItems = themeStringItems,
                    allReadBehaviorStringItems = allReadBehaviorStringItems,
                    allReadBehaviorItems = allReadBehaviorItems,
                    launchTabItems = launchTabItems,
                    launchTabStringItems = launchTabStringItems,
                    swipeDirectionItems = swipeDirectionItems,
                    swipeDirectionStringItems = swipeDirectionStringItems
            )
        }
    }
}