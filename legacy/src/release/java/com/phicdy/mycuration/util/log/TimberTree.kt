package com.phicdy.mycuration.util.log

import timber.log.Timber
import javax.inject.Inject

class TimberTree @Inject constructor() : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    }
}