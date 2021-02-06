package com.phicdy.mycuration.core

interface ActionCreator4<T1, T2, T3, T4> {
    suspend fun run(arg1: T1, arg2: T2, arg3: T3, arg4: T4)
}