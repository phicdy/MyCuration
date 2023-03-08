package com.phicdy.mycuration.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {
    @ActivityScoped
    @Provides
    fun provideCoroutineScope(@ActivityContext activity: Context): CoroutineScope =
            activity as CoroutineScope

    @ActivityScoped
    @Provides
    fun provideCoroutineContext(): CoroutineContext = Dispatchers.Main
}