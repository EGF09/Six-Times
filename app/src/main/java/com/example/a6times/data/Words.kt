package com.example.a6times.data

//region Words Data Classes
data class Words(
    var wordID: Int = 0,
    val engWordName: String = "",
    val turWordName: String = "",
    val category: String = "",
    val picture: String = "",
    var isActive: Boolean = true,
    var progress: Int = 0,
    var lastReviewedAt: Long = 0L,
    var nextReviewAt: Long = 0L,
    var isLearned: Boolean = false
)
//endregion