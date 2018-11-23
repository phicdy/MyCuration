package com.phicdy.mycuration.util.log

import timber.log.Timber

class TimberTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    }
}