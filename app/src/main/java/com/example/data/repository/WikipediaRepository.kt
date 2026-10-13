package com.example.data.repository

import com.example.data.model.WikipediaArticle
import com.example.data.model.WikipediaSearchResponse
import com.example.data.model.WikipediaSummaryResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class WikipediaRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val searchAdapter = moshi.adapter(WikipediaSearchResponse::class.java)
    private val summaryAdapter = moshi.adapter(WikipediaSummaryResponse::class.java)

    private val userAgent = "MySmartAssistant/5.0 (HamzaAI-Android; hamzaalidina193@gmail.com)"

    suspend fun searchWikipedia(
        baseUrl: String,
        query: String,
        limit: Int = 8
    ): List<String> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = baseUrl.toHttpUrlOrNull()?.newBuilder() ?: return@withContext emptyList()
            val url = urlBuilder
                .addQueryParameter("action", "query")
                .addQueryParameter("list", "search")
                .addQueryParameter("srsearch", query)
                .addQueryParameter("srwhat", "text")
                .addQueryParameter("format", "json")
                .addQueryParameter("utf8", "1")
                .addQueryParameter("srlimit", limit.toString())
                .build()

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val bodyString = response.body?.string() ?: return@withContext emptyList()
                val parsed = searchAdapter.fromJson(bodyString)
                val items = parsed?.query?.search ?: emptyList()
                items.mapNotNull { it.title?.takeIf { t -> t.isNotBlank() } }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun fetchSummary(
        baseUrl: String,
        title: String
    ): WikipediaArticle? = withContext(Dispatchers.IO) {
        try {
            val encodedTitle = URLEncoder.encode(title.replace(" ", "_"), StandardCharsets.UTF_8.toString())
            // baseUrl like https://en.wikipedia.org/api/rest_v1/page/summary/
            val fullUrl = if (baseUrl.endsWith("/")) "$baseUrl$encodedTitle" else "$baseUrl/$encodedTitle"

            val request = Request.Builder()
                .url(fullUrl)
                .header("User-Agent", userAgent)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyString = response.body?.string() ?: return@withContext null
                val parsed = summaryAdapter.fromJson(bodyString)
                val summary = parsed?.extract?.trim().orEmpty()
                if (summary.isBlank()) return@withContext null

                val desktopUrl = parsed?.contentUrls?.desktop?.page.orEmpty().ifBlank {
                    "https://en.wikipedia.org/wiki/$encodedTitle"
                }

                WikipediaArticle(
                    title = parsed?.title ?: title,
                    summary = summary,
                    url = desktopUrl
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
