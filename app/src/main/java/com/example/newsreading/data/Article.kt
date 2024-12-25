package com.example.newsreading.data

import java.io.Serializable

data class Article(
    val title: String,
    val author: String,
    val description: String,
    val url: String
) : Serializable