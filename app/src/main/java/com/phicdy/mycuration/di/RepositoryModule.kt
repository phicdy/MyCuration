package com.phicdy.mycuration.di

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.phicdy.mycuration.data.db.DatabaseHelper
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideSQLiteDatabase(@ApplicationContext context: Context): SQLiteDatabase =
            DatabaseHelper(context = context).writableDatabase

    @Singleton
    @Provides
    fun provideArticleRepository(sqLiteDatabase: SQLiteDatabase): ArticleRepository =
            ArticleRepository(sqLiteDatabase)

    @Singleton
    @Provides
    fun provideFilterRepository(sqLiteDatabase: SQLiteDatabase): FilterRepository =
            FilterRepository(sqLiteDatabase)

    @Singleton
    @Provides
    fun provideRssRepository(
            sqLiteDatabase: SQLiteDatabase,
            articleRepository: ArticleRepository,
            filterRepository: FilterRepository
    ): RssRepository =
            RssRepository(sqLiteDatabase, articleRepository, filterRepository)

    @Singleton
    @Provides
    fun providePreferenceHelper(): PreferenceHelper = PreferenceHelper
}