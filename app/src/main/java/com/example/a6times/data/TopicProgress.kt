package com.example.a6times.data

/**
 * Bir konunun öğrenme ilerlemesini temsil eden veri sınıfı.
 * 
 * @property topic Konunun adı.
 * @property correctCount Doğru cevaplanan kelime sayısı.
 * @property totalCount Konudaki toplam kelime sayısı.
 * @property progressPercentage Konunun tamamlanma yüzdesi.
 */
data class TopicProgress(
    val topic: String,
    val correctCount: Int,
    val totalCount: Int,
    val progressPercentage: Int
)
