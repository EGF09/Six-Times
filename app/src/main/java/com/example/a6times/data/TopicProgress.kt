package com.example.a6times.data

data class TopicProgress(
    val topic: String,
    val correctCount: Int,
    val totalCount: Int,
    val progressPercentage: Int
)
