package com.phicdy.mycuration.di

import com.phicdy.mycuration.admob.AdmobProvider
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.core.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Singleton
    @Provides
    fun provideDispatcher(): Dispatcher = Dispatcher()

    @Singleton
    @Provides
    fun provideAdProvider(): AdProvider = AdmobProvider()
}