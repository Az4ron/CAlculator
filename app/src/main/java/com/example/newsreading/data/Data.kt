package com.example.newsreading.data

import android.content.Context
import android.util.Log
import android.webkit.WebView
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

const val API_KEY2 = "fea87b7e789746029e9b197dd57d8a74"

@Database(entities = [Follow::class], version = 4)
abstract class Data : RoomDatabase() {
    abstract fun getDao(): Dao

    companion object {
        fun getDatabase(context: Context): Data {
            return Room.databaseBuilder(context.applicationContext, Data::class.java, "Follow.db")
                .fallbackToDestructiveMigration()
                .build()
        }

        private fun fetchNewsData(url: String, webView: WebView) {
            val queue = Volley.newRequestQueue(webView.context)

            val stringRequest = object : StringRequest(Request.Method.GET, url,
                { response ->
                    Log.d("MyLog", "Ответ API: $response") // Логируем ответ
                    try {
                        val jsonResponse = JSONObject(response)
                        val articles = jsonResponse.getJSONArray("articles")

                        val stringBuilder = StringBuilder()
                        stringBuilder.append("<html><body>")
                        for (i in 0 until articles.length()) {
                            val article = articles.getJSONObject(i)
                            val author = article.optString("author", "Неизвестный автор")
                            val title = article.optString("title", "Неизвестное название")
                            val description = article.optString("description", "Нет заголовка")
                            val articleUrl = article.optString("url", "")

                            // Используем onclick для открытия статьи и oncontextmenu для добавления в избранное
                            stringBuilder.append("<h3><a href=\"$articleUrl\" style=\"text-decoration:none; color:black;\" " +
                                    "onclick=\"window.open('$articleUrl', '_blank'); return false;\" " +
                                    "oncontextmenu=\"Android.onLongPress('$author', '$title', '$description', '$articleUrl'); return false;\">$title</a></h3>")
                            stringBuilder.append("<p><strong>$description</strong> - $author</p>")
                            stringBuilder.append("<hr>")
                        }

                        stringBuilder.append("</body></html>")
                        webView.loadData(stringBuilder.toString(), "text/html", "UTF-8")
                    } catch (e: Exception) {
                        Log.e("MyLog", "Ошибка парсинга JSON: ${e.message}")
                        webView.loadData("Ошибка парсинга данных", "text/html", "UTF-8")
                    }
                },
                { error ->
                    Log.e("MyLog", "Ошибка Volley: ${error.message}")
                    webView.loadData("Ошибка: ${error.message ?: "Неизвестная ошибка"}", "text/html", "UTF-8")
                }
            ) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
                    return headers
                }
            }
            queue.add(stringRequest)
        }

        fun getAppleData(webView: WebView) {
            val url = "https://newsapi.org/v2/everything?q=apple&from=2024-12-23&to=2024-12-23&sortBy=popularity&apiKey=$API_KEY2"
            fetchNewsData(url, webView)
        }

        fun getTechCrunchData(webView: WebView) {
            val url = "https://newsapi.org/v2/top-headlines?sources=techcrunch&apiKey=$API_KEY2"
            fetchNewsData(url, webView)
        }

        fun getTeslaData(webView: WebView) {
            val url = "https://newsapi.org/v2/everything?q=tesla&from=2024-11-25&sortBy=publishedAt&apiKey=$API_KEY2"
            fetchNewsData(url, webView)
        }

        fun getUSRightNowData(webView: WebView) {
            val url = "https://newsapi.org/v2/top-headlines?country=us&category=business&apiKey=$API_KEY2"
            fetchNewsData(url, webView)
        }

        fun getWallStreetJournalData(webView: WebView) {
            val url = "https://newsapi.org/v2/everything?domains=wsj.com&apiKey=$API_KEY2"
            fetchNewsData(url, webView)
        }
    }
}