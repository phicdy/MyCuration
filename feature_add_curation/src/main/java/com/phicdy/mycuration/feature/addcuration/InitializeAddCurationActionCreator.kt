package com.phicdy.mycuration.feature.addcuration

import com.phicdy.mycuration.core.ActionCreator1
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.CurationRepository
import javax.inject.Inject

class InitializeAddCurationActionCreator @Inject constructor(
        private val repository: CurationRepository,
        private val dispatcher: Dispatcher
) : ActionCreator1<Int> {
    override suspend fun run(arg: Int) {
        dispatcher.dispatch(InitializeAddCurationAction(AddCurationState.Loading))
        if (arg != NOT_EDIT_CURATION_ID) {
            val curationName = repository.getCurationNameById(arg)
            val words = repository.getCurationWords(arg)
            dispatcher.dispatch(InitializeAddCurationAction(AddCurationState.Loaded(words, curationName, "")))
        } else {
            dispatcher.dispatch(InitializeAddCurationAction(AddCurationState.Loaded(emptyList(), "", "")))
        }
    }

    companion object {
        const val NOT_EDIT_CURATION_ID = -1
    }
}