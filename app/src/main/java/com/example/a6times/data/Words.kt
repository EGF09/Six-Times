package com.example.a6times.data

data class Words(
    var wordID: Int = 0,
    val engWordName: String = "",
    val turWordName: String = "",
    val category: String = "",
    val picture: String = "",
    val isActive: Boolean = true
)