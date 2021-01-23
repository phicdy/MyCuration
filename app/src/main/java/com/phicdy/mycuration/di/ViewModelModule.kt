package com.phicdy.mycuration.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    @ViewModelScoped
    @Provides
    fun provideCoroutineContext(): CoroutineContext = Dispatchers.Main
}