package com.phicdy.action.articlelist

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.entity.UnReadArticle

data class UnReadArticleAction(
        override val value: UnReadArticle
) : Action<UnReadArticle>