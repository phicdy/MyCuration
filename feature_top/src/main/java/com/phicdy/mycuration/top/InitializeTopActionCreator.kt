package com.phicdy.mycuration.top

import com.phicdy.mycuration.core.ActionCreator
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.RssRepository
import javax.inject.Inject

class InitializeTopActionCreator @Inject constructor(
    private val rssRepository: RssRepository,
    private val dispatcher: Dispatcher
): ActionCreator {
    override suspend fun run() {
        val numOfRss = rssRepository.getNumOfRss()
        dispatcher.dispatch(InitializeTopAction(InitializeTopValue(numOfRss)))
    }
}