package com.phicdy.mycuration.articlelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phicdy.mycuration.articlelist.action.ScrollActionCreator
import com.phicdy.mycuration.articlelist.ui.ArticleListUiState
import com.phicdy.mycuration.core.Dispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleListViewModel @Inject constructor(
        private val scrollActionCreator: ScrollActionCreator,
        private val dispatcher: Dispatcher
): ViewModel() {

    private val _channel = Channel<Interation>(Channel.UNLIMITED)
    val interationChannel: Flow<Interation> = _channel.receiveAsFlow()

    private val _binding = MutableStateFlow<ArticleListUiBinding>(ArticleListUiBinding.Init)
    val binding: StateFlow<ArticleListUiBinding> = _binding

    private val mutableUiState = MutableStateFlow<ArticleListUiState>(ArticleListUiState.Loading)

    /**
     * Immutable snapshot of the list for rendering. Re-emitted after every action that changes
     * an item, including in-place status mutations by action creators.
     */
    val uiState: StateFlow<ArticleListUiState> = mutableUiState

    private val reducer = ArticleListReducer(viewModelScope, _channel, _binding, mutableUiState)

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