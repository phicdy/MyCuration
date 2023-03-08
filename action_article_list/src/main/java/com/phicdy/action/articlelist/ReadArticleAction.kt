package com.phicdy.action.articlelist

import com.phicdy.mycuration.core.Action
import com.phicdy.mycuration.entity.ReadArticle

data class ReadArticleAction(
        override val value: ReadArticle
) : Action<ReadArticle>
