package com.phicdy.mycuration.core

interface Action<out T> {
    val type: String
    val value: T
}