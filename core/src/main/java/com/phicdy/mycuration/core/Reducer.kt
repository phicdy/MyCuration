package com.phicdy.mycuration.core

interface Reducer {
    fun reduce(action: Action<*>)
}
