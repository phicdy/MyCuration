package com.phicdy.mycuration.domain.rss

import com.phicdy.mycuration.domain.util.DateParser
import com.phicdy.mycuration.domain.util.TextUtil
import com.phicdy.mycuration.entity.Article
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class SAXRssHandler : DefaultHandler() {
    private var currentItem: Article? = null
    private var currentElement: String? = null
    private val rssItems = mutableListOf<Article>()

    fun getArticles(): List<Article> {
        return rssItems
    }

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?
    ) {
        currentElement = localName
        when (qName) {
            "item", "entry" -> {
                currentItem =
                    Article(0, "", "", Article.UNREAD, Article.DEDAULT_HATENA_POINT, 0, 0, "", "")
            }

            "link" -> {
                // For RSS 1.0 & 2.0
                val href = parseAtomAriticleUrl(attributes)
                if (href.isNotBlank() && href != "\n") {
                    // Atom
                    currentItem?.url = href
                }
            }
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        if (ch == null || currentItem == null) return
        val value = String(ch, start, length)
        when (currentElement) {
            "title" -> {
                currentItem?.title += TextUtil.removeLineFeed(value).trimEnd()
            }

            "link" -> {
                currentItem?.url += value.trimEnd()
            }

            "date", "pubDate", "published", "updated" -> {
                if (currentItem?.postedDate == 0L) {
                    currentItem?.postedDate = DateParser.changeToJapaneseDate(value)
                }
            }
        }
    }

    /**
     * Parse URL from link tag
     *
     * ATOM: <link rel='alternate' type='text/html' href='http://xxxx' title='yyyy'></link>
     *
     * @param attributes Attributes
     * @return URL or empty string
     */
    private fun parseAtomAriticleUrl(attributes: Attributes?): String {
        if (attributes == null) return ""

        var isTypeTextHtml = false
        var hasType = false
        var href = ""
        for (i in 0 until attributes.length) {
            val attributeName = attributes.getLocalName(i)
            val attributeValue = attributes.getValue(i)
            if (attributeName == "type") {
                hasType = true
                if (attributeValue == "text/html") {
                    isTypeTextHtml = true
                }
                continue
            }
            if (attributeName == "href") {
                if (attributeValue.startsWith("http://") ||
                    attributeValue.startsWith("https://")
                ) {
                    href = attributeValue
                }
            }
        }
        return if (!hasType || isTypeTextHtml) href else ""
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        when (qName) {
            "item", "entry" -> {
                if (currentItem == null) return
                rssItems.add(currentItem!!)
                currentElement = null
            }
        }
    }
}