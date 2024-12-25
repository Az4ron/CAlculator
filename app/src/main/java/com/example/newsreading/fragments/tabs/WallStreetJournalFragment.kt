package com.example.newsreading.fragments.tabs

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.newsreading.R
import com.example.newsreading.data.Data
import com.example.newsreading.data.Follow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallStreetJournalFragment : Fragment() {

    private lateinit var webView: WebView
    private var currentArticleAuthor: String? = null
    private var currentArticleTitle: String? = null
    private var currentArticleDescription: String? = null
    private var currentArticleUrl: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wall_street_journal, container, false)
        webView = view.findViewById(R.id.wallStreetJournalData)

        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.javaScriptEnabled = true

        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onLongPress(author: String, title: String, description: String, url: String) {
                // Вызов метода onLongPress
                this@WallStreetJournalFragment.onLongPress(author, title, description, url)
            }
        }, "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e("WebViewError", "Ошибка: $description")
                webView.loadData("Ошибка загрузки: $description", "text/html", "UTF-8")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                currentArticleUrl = url
            }
        }

        // Получение данных о новостях
        Data.getWallStreetJournalData(webView)

        registerForContextMenu(webView)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.destroy()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                requireActivity().finish()
            }
        }
    }

//    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
//        super.onCreateContextMenu(menu, v, menuInfo)
//        menu.setHeaderTitle("Выберите действие")
//        menu.add(0, v.id, 0, "Добавить в закладки")
//    }
//
//    override fun onContextItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            webView.id -> {
//                // Проверяем, что все необходимые данные инициализированы
//                if (currentArticleAuthor != null && currentArticleTitle != null && currentArticleDescription != null && currentArticleUrl != null) {
//                    val author = currentArticleAuthor!!
//                    val title = currentArticleTitle!!
//                    val description = currentArticleDescription!!
//                    val url = currentArticleUrl!!
//
//                    Log.d("BookmarkFragment", "Сохраняем статью: Автор='$author', Заголовок='$title', Описание ='$description', URL='$url'")
//                    Toast.makeText(requireContext(), "Заголовок: $title, URL: $url", Toast.LENGTH_SHORT).show()
//                    addToFavorites(author, title, description, url)
//                } else {
//                    Log.e("BookmarkFragment", "Не удалось получить данные статьи.")
//                }
//                true
//            }
//            else -> super.onContextItemSelected(item)
//        }
//    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle("Выберите действие")
        menu.add(0, v.id, 0, "Добавить в закладки")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.title) {
            "Добавить в закладки" -> {
                if (currentArticleAuthor != null && currentArticleTitle != null && currentArticleDescription != null && currentArticleUrl != null) {
                    val follow = Follow(
                        title = currentArticleTitle!!,
                        description = currentArticleDescription!!,
                        author = currentArticleAuthor!!,
                        url = currentArticleUrl!!
                    )
                    addToFavorites(follow)
                } else {
                    Log.e("BookmarkFragment", "Не удалось получить данные статьи.")
                    Toast.makeText(requireContext(), "Не удалось получить данные статьи.", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

//    override fun onContextItemSelected(item: MenuItem): Boolean {
//        return when (item.title) {
//            "Добавить в закладки" -> {
//                if (currentArticleAuthor != null && currentArticleTitle != null && currentArticleDescription != null && currentArticleUrl != null) {
//                    addToFavorites(currentArticleAuthor!!, currentArticleTitle!!, currentArticleDescription!!, currentArticleUrl!!)
//                } else {
//                    Log.e("BookmarkFragment", "Не удалось получить данные статьи.")
//                    Toast.makeText(requireContext(), "Не удалось получить данные статьи.", Toast.LENGTH_SHORT).show()
//                }
//                true
//            }
//            else -> super.onContextItemSelected(item)
//        }
//    }

    @JavascriptInterface
    fun onLongPress(author: String, title: String, description: String, url: String) {
        currentArticleAuthor = author
        currentArticleTitle = title
        currentArticleDescription = description
        currentArticleUrl = url
    }

//    private fun addToFavorites(author: String, title: String, description: String, url: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val db = Data.getDatabase(requireContext())
//            val existingFollow = db.getDao().getFollowByUrl(url)
//
//            if (existingFollow == null) {
//                val follow = Follow(title = title, description = description, author = author, url = url)
//                db.getDao().insertData(follow)
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(requireContext(), "Статья добавлена в закладки!", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(requireContext(), "Статья уже добавлена в закладки!", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

    private fun addToFavorites(follow: Follow) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = Data.getDatabase(requireContext())
            Log.d("BookmarkFragment", "Добавление: $follow")
            db.getDao().insertData(follow)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Статья добавлена в закладки!", Toast.LENGTH_SHORT).show()
                loadBookmarks()
            }
        }
    }

    private fun loadBookmarks() {
        lifecycleScope.launch {
            val db = Data.getDatabase(requireContext())
            db.getDao().getAllData().collect { bookmarks ->
                // Обработка полученных закладок
                // Например, обновление WebView или RecyclerView
            }
        }
    }
}