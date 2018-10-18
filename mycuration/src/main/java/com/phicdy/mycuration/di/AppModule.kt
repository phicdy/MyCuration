package com.phicdy.mycuration.di

import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.presentation.presenter.TopActivityPresenter
import com.phicdy.mycuration.presentation.view.TopActivityView
import com.phicdy.mycuration.util.PreferenceHelper
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.module


val appModule = module {

    single<SQLiteDatabase> { DatabaseHelper(androidApplication()).writableDatabase }
    single { RssRepository(get()) }
    single { ArticleRepository(get()) }

    scope("top") { (view: TopActivityView) ->
        TopActivityPresenter(
                launchTab = PreferenceHelper.launchTab,
                view = view,
                articleRepository = get(),
                rssRepository = get()
        )
    }
}