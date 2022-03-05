package com.phicdy.mycuration.feature.addcuration

import com.phicdy.mycuration.core.ActionCreator3
import com.phicdy.mycuration.core.Dispatcher
import com.phicdy.mycuration.data.repository.CurationRepository
import javax.inject.Inject

class StoreCurationActionCreator @Inject constructor(
        private val dispatcher: Dispatcher,
        private val repository: CurationRepository
): ActionCreator3<String, List<String>, Int> {
    override suspend fun run(name: String, words: List<String>, updateId: Int) {
        dispatcher.dispatch(StoreCurationAction(StoreCurationState.Loading))
        if (name.isEmpty()) {
            dispatcher.dispatch(StoreCurationAction(StoreCurationState.EmptyNameError))
            return
        }
        if (words.isEmpty()) {
            dispatcher.dispatch(StoreCurationAction(StoreCurationState.EmptyWordError))
            return
        }

        val isNew = updateId < 0
        if (isNew && repository.isExist(name)) {
            dispatcher.dispatch(StoreCurationAction(StoreCurationState.SameNameExitError))
            return
        }
        if (isNew) {
            val id = repository.store(name, words)
            repository.adaptToArticles(id.toInt(), words)
            dispatcher.dispatch(StoreCurationAction(StoreCurationState.SucceedToAdd))
        } else {
            repository.update(updateId, name, words)
            repository.adaptToArticles(updateId, words)
            dispatcher.dispatch(StoreCurationAction(StoreCurationState.SucceedToEdit))
        }
    }
}