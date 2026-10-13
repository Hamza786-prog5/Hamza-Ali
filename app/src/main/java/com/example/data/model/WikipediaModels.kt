package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WikipediaSearchResponse(
    val query: WikipediaQuery? = null
)

@JsonClass(generateAdapter = true)
data class WikipediaQuery(
    val search: List<WikipediaSearchItem>? = null
)

@JsonClass(generateAdapter = true)
data class WikipediaSearchItem(
    val title: String? = null,
    val pageid: Long? = null,
    val snippet: String? = null
)

@JsonClass(generateAdapter = true)
data class WikipediaSummaryResponse(
    val title: String? = null,
    val extract: String? = null,
    val description: String? = null,
    @Json(name = "content_urls")
    val contentUrls: WikipediaContentUrls? = null
)

@JsonClass(generateAdapter = true)
data class WikipediaContentUrls(
    val desktop: WikipediaDesktopUrl? = null
)

@JsonClass(generateAdapter = true)
data class WikipediaDesktopUrl(
    val page: String? = null
)
