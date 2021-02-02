package com.phicdy.mycuration.articlelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phicdy.mycuration.articlelist.action.ScrollActionCreator
import com.phicdy.mycuration.core.Dispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleListViewModel @Inject constructor(
        private val scrollActionCreator: ScrollActionCreator,
        private val dispatcher: Dispatcher
): ViewModel() {

    private val _channel = Channel<Interation>(Channel.UNLIMITED)
    val interationChannel : Flow<Interation> = _channel.receiveAsFlow()

    private val _binding = MutableLiveData<ArticleListUiBinding>()
    val binding : LiveData<ArticleListUiBinding> = _binding

    private val reducer = ArticleListReducer(viewModelScope, _channel, _binding)

    init {
        dispatcher.register(reducer)
    }

    fun onFabButtonClicked(
            findFirstVisibleItemPosition: Int,
            findLastCompletelyVisibleItemPosition: Int,
            currentList: List<ArticleItem>
    ) {
        viewModelScope.launch {
            scrollActionCreator.run(
                    findFirstVisibleItemPosition,
                    findLastCompletelyVisibleItemPosition,
                    currentList
            )
        }
    }

    override fun onCleared() {
        dispatcher.unregister(reducer)
        super.onCleared()
    }
}