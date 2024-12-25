package com.example.newsreading.fragments.navigation

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.newsreading.R
import com.example.newsreading.data.Data
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarkFragment : Fragment() {

    private lateinit var webView: WebView
    private var currentArticleUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bookmark, container, false)
        webView = view.findViewById(R.id.bookmarks)

        // Настройка WebView
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                currentArticleUrl = url // Сохраняем текущий URL статьи
            }
        }

        loadBookmarks()

        // Регистрируем WebView для контекстного меню
        registerForContextMenu(webView)

        return view
    }

    private fun loadBookmarks() {
        lifecycleScope.launch {
            val db = Data.getDatabase(requireContext())
            db.getDao().getAllData().collect { bookmarks ->
                // Форматируем данные в HTML
                val stringBuilder = StringBuilder()
                stringBuilder.append("<html><body>")
                for (bookmark in bookmarks) {
                    val title = bookmark.title ?: "Неизвестное название"
                    val content = bookmark.description ?: "Нет контента"
                    val articleUrl = bookmark.url ?: "#"

                    // Добавляем заголовок с обработчиком нажатий
                    stringBuilder.append("<h3><a href=\"$articleUrl\" style=\"text-decoration:none; color:black;\">$title</a></h3>")
                    stringBuilder.append("<p>$content</p>")
                    stringBuilder.append("<hr>")
                }
                stringBuilder.append("</body></html>")

                webView.loadData(stringBuilder.toString(), "text/html", "UTF-8")
            }
        }
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

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle("Выберите действие")
        menu.add(0, v.id, 0, "Удалить из закладок")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.title) {
            "Удалить из закладок" -> {
                currentArticleUrl?.let {
                    Log.d("BookmarkFragment", "Удаление URL: $it")
                    removeFromFavorites(it)
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

//    private fun removeFromFavorites(url: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val db = Data.getDatabase(requireContext())
//
//            Log.d("BookmarkFragment", "Пытаемся удалить URL: $url")
//
//            // Проверка, существует ли запись перед удалением
//            val bookmarkExists = db.getDao().getAllData().first().any { it.url == url }
//            Log.d("BookmarkFragment", "Существует ли запись: $bookmarkExists")
//
//            if (bookmarkExists) {
//                db.getDao().deleteByUrl(url)
//                Log.d("BookmarkFragment", "Запись удалена")
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(requireContext(), "Статья удалена из закладок!", Toast.LENGTH_SHORT).show()
//                    loadBookmarks()
//                    currentArticleUrl = null
//                }
//            } else {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(requireContext(), "Статья не найдена в закладках!", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

    private fun removeFromFavorites(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = Data.getDatabase(requireContext())
            Log.d("BookmarkFragment", "Удаление URL: $url")
            db.getDao().deleteByUrl(url)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Статья удалена из закладок!", Toast.LENGTH_SHORT).show()
                loadBookmarks()
            }
        }
    }
}