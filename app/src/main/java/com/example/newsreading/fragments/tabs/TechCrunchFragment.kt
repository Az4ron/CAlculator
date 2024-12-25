package com.example.newsreading.fragments.tabs

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.newsreading.R
import com.example.newsreading.data.Data

class TechCrunchFragment : Fragment() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tech_crunch, container, false)
        webView = view.findViewById(R.id.techCrunchData)
        webView.getSettings().javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e("WebViewError", "Ошибка: $description")
                webView.loadData("Ошибка загрузки: $description", "text/html", "UTF-8")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("WebView", "Страница загружена: $url")
            }
        }

        Data.getTechCrunchData(webView)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView.destroy() // Освобождаем ресурсы WebView
    }

    override fun onResume() {
        super.onResume()
        // Включаем возможность возврата по истории WebView
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (webView.canGoBack()) {
                webView.goBack() // Возврат к предыдущей странице
            } else {
                requireActivity().finish() // Закрыть фрагмент, если нет истории
            }
        }
    }}