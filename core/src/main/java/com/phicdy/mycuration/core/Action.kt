package com.phicdy.mycuration.core

interface Action<out T> {
    val value: T
}