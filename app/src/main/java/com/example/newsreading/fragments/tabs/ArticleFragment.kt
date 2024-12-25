package com.example.newsreading.fragments.tabs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.example.newsreading.R
import com.example.newsreading.data.Article

class ArticleFragment : Fragment() {

    private lateinit var article: Article
    private lateinit var webView: WebView

    companion object {
        private const val ARG_ARTICLE = "article"

        fun newInstance(article: Article): ArticleFragment {
            val fragment = ArticleFragment()
            val args = Bundle()
            args.putSerializable(ARG_ARTICLE, article)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        article = arguments?.getSerializable(ARG_ARTICLE) as Article
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_article, container, false)
        webView = view.findViewById(R.id.detailData)
        webView.loadUrl(article.url)
        return view
    }
}