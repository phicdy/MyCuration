package com.phicdy.mycuration.core

interface ActionCreator3<T1, T2, T3> {
    suspend fun run(arg1: T1, arg2: T2, arg3: T3)
}