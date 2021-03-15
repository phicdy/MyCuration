package com.phicdy.mycuration.di

import android.database.sqlite.SQLiteDatabase
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
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideSQLiteDatabase(databaseHelper: DatabaseHelper): SQLiteDatabase = databaseHelper.writableDatabase

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
    fun provideCurationRepository(sqLiteDatabase: SQLiteDatabase): CurationRepository =
            CurationRepository(sqLiteDatabase)

    @Singleton
    @Provides
    fun provideFavoriteRepository(sqLiteDatabase: SQLiteDatabase): FavoriteRepository =
            FavoriteRepository(sqLiteDatabase)

    @Singleton
    @Provides
    fun providePreferenceHelper(): PreferenceHelper = PreferenceHelper

    @Singleton
    @Provides
    fun provideNetworkTaskManager(
            articleRepository: ArticleRepository,
            rssRepository: RssRepository,
            curationRepository: CurationRepository,
            filterRepository: FilterRepository,
            okHttpClient: OkHttpClient
    ): NetworkTaskManager = NetworkTaskManager(articleRepository, rssRepository, curationRepository, filterRepository, okHttpClient)

    @Singleton
    @Provides
    fun provideAdditionalSettingApi(
            rssRepository: RssRepository,
            alarmManagerTaskManager: AlarmManagerTaskManager
    ): AdditionalSettingApi = AdditionalSettingRepository(rssRepository, alarmManagerTaskManager)

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()
}