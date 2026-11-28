package com.phicdy.mycuration.domain.rss

import java.net.URL

/**
 * Resolves [href] found in the page at this URL, keeping the port.
 * [href] can be absolute ("https://example.com/feed"), protocol relative ("//example.com/feed"),
 * path only ("/feed") or relative to the page ("feed").
 */
internal fun URL.resolve(href: String): String = URL(this, href).toString()

/**
 * Converts this URL to the site URL that consists of the protocol, host and port, e.g. "https://example.com".
 */
internal fun URL.toSiteUrl(): String = URL(protocol, host, port, "").toString()
