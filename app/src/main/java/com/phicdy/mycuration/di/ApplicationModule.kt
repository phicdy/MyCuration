package com.phicdy.mycuration.di

import android.app.Application
import com.phicdy.mycuration.MyApplication
import com.phicdy.mycuration.admob.AdmobProvider
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.di.common.ApplicationCoroutineScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
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

    @ApplicationCoroutineScope
    @Provides
    fun provideApplicationCoroutineScope(application: Application): CoroutineScope = (application as MyApplication).applicationScope
}