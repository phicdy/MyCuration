package com.phicdy.mycuration.di

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.R
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.repository.AdditionalSettingApi
import com.phicdy.mycuration.data.repository.AdditionalSettingRepository
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.data.repository.UnreadCountRepository
import com.phicdy.mycuration.domain.alarm.AlarmManagerTaskManager
import com.phicdy.mycuration.domain.rss.RssParseExecutor
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.presentation.presenter.AddCurationPresenter
import com.phicdy.mycuration.presentation.presenter.ArticleListPresenter
import com.phicdy.mycuration.presentation.presenter.CurationListPresenter
import com.phicdy.mycuration.presentation.presenter.FeedSearchPresenter
import com.phicdy.mycuration.presentation.presenter.FeedUrlHookPresenter
import com.phicdy.mycuration.presentation.presenter.FilterListPresenter
import com.phicdy.mycuration.presentation.presenter.RegisterFilterPresenter
import com.phicdy.mycuration.presentation.presenter.RssListPresenter
import com.phicdy.mycuration.presentation.presenter.SettingPresenter
import com.phicdy.mycuration.presentation.presenter.TopActivityPresenter
import com.phicdy.mycuration.presentation.view.AddCurationView
import com.phicdy.mycuration.presentation.view.CurationListView
import com.phicdy.mycuration.presentation.view.FeedSearchView
import com.phicdy.mycuration.presentation.view.FeedUrlHookView
import com.phicdy.mycuration.presentation.view.RegisterFilterView
import com.phicdy.mycuration.presentation.view.RssListView
import com.phicdy.mycuration.presentation.view.SettingView
import com.phicdy.mycuration.presentation.view.TopActivityView
import com.phicdy.mycuration.presentation.view.activity.FeedSearchActivity
import com.phicdy.mycuration.presentation.view.activity.FeedUrlHookActivity
import com.phicdy.mycuration.presentation.view.activity.RegisterFilterActivity
import com.phicdy.mycuration.presentation.view.activity.TopActivity
import com.phicdy.mycuration.presentation.view.fragment.AddCurationFragment
import com.phicdy.mycuration.presentation.view.fragment.ArticlesListFragment
import com.phicdy.mycuration.presentation.view.fragment.CurationListFragment
import com.phicdy.mycuration.presentation.view.fragment.FilterListFragment
import com.phicdy.mycuration.presentation.view.fragment.RssListFragment
import com.phicdy.mycuration.presentation.view.fragment.SettingFragment
import com.phicdy.mycuration.util.PreferenceHelper
import com.phicdy.mycuration.util.log.TimberTree
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module


val appModule = module {

    single<SQLiteDatabase> { DatabaseHelper(androidApplication()).writableDatabase }
    single { RssRepository(get(), get(), get()) }
    single { ArticleRepository(get()) }
    single { CurationRepository(get()) }
    single { FilterRepository(get()) }
    single { PreferenceHelper }
    single { NetworkTaskManager(get(), get(), get(), get(), get()) }
    single { UnreadCountRepository(get(), get()) }
    single<AdditionalSettingApi> { AdditionalSettingRepository(get(), get()) }
    single { AlarmManagerTaskManager(androidContext()) }

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
        scoped { (view: RssListView) ->
            RssListPresenter(
                    view = view,
                    preferenceHelper = get(),
                    rssRepository = get(),
                    networkTaskManager = get(),
                    unreadCountRepository = get()
            )
        }
    }

    scope(named<ArticlesListFragment>()) {
        scoped { (feedId: Int, curationId: Int, query: String, action: String) ->
            ArticleListPresenter(
                    feedId = feedId,
                    curationId = curationId,
                    rssRepository = get(),
                    preferenceHelper = get(),
                    articleRepository = get(),
                    unreadCountRepository = get(),
                    query = query,
                    action = action
            )
        }
    }

    scope(named<CurationListFragment>()) {
        scoped { (view: CurationListView) ->
            CurationListPresenter(
                    view = view,
                    rssRepository = get(),
                    curationRepository = get(),
                    unreadCountRepository = get()
            )
        }
    }

    scope(named<FeedSearchActivity>()) {
        scoped { (view: FeedSearchView) ->
            FeedSearchPresenter(
                    view = view,
                    rssRepository = get(),
                    networkTaskManager = get(),
                    executor = RssParseExecutor(RssParser(), get())
            )
        }
    }

    scope(named<FeedUrlHookActivity>()) {
        scoped { (view: FeedUrlHookView, action: String, dataString: String, extrasText: String) ->
            FeedUrlHookPresenter(
                    view = view,
                    rssRepository = get(),
                    networkTaskManager = get(),
                    action = action,
                    dataString = dataString,
                    extrasText = extrasText,
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