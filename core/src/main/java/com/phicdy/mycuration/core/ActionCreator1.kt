package com.phicdy.mycuration.core

interface ActionCreator1<T> {
    suspend fun run(arg: T)
}