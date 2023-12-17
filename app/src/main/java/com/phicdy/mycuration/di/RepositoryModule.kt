package com.phicdy.mycuration.di

import android.content.Context
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.AdditionalSettingApi
import com.phicdy.mycuration.data.repository.AdditionalSettingRepository
import com.phicdy.mycuration.data.repository.ArticleRepository
import com.phicdy.mycuration.data.repository.CurationRepository
import com.phicdy.mycuration.data.repository.FilterRepository
import com.phicdy.mycuration.data.repository.RssRepository
import com.phicdy.mycuration.domain.rss.RssParser
import com.phicdy.mycuration.domain.task.NetworkTaskManager
import com.phicdy.mycuration.repository.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver
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
    fun provideDatabase(
            @ApplicationContext context: Context,
    ): Database = Database(
            AndroidSqliteDriver(
                    schema = Database.Schema,
                    context = context,
                    name = "rss_manage"
            )
    )

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
    ): NetworkTaskManager = NetworkTaskManager(
        articleRepository,
        rssRepository,
        curationRepository,
        filterRepository,
        okHttpClient,
        RssParser()
    )

    @Singleton
    @Provides
    fun provideAdditionalSettingApi(
        rssRepository: RssRepository,
        articleRepository: ArticleRepository
    ): AdditionalSettingApi = AdditionalSettingRepository(rssRepository, articleRepository)

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()
}