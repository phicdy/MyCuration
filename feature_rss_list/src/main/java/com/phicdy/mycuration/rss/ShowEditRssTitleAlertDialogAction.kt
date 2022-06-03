package com.phicdy.mycuration.rss

import com.phicdy.mycuration.core.Action

data class ShowEditRssTitleAlertDialogAction(override val value: Int, val title: String) :
    Action<Int>
