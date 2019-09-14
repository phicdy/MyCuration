package com.phicdy.mycuration.core

interface ActionCreator2<T1, T2> {
    suspend fun run(arg1: T1, arg2: T2)
}