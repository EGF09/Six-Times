package com.example.a6times.data

//region Words Data Classes
/**
 * Kelime bilgilerini ve öğrenme durumunu temsil eden veri sınıfı.
 * 
 * @property wordID Kelimenin benzersiz kimliği.
 * @property engWordName Kelimenin İngilizce karşılığı.
 * @property turWordName Kelimenin Türkçe karşılığı.
 * @property category Kelimenin ait olduğu kategori (örn: Hayvanlar, Fiiller).
 * @property picture Kelime ile ilişkili resmin yolu veya URL'si.
 * @property isActive Kelimenin şu anki çalışma listesinde olup olmadığı.
 * @property progress Kelimenin öğrenme aşaması (0-6 arası).
 * @property lastReviewedAt Kelimenin en son gözden geçirildiği zaman damgası.
 * @property nextReviewAt Kelimenin bir sonraki gözden geçirme zaman damgası.
 * @property isLearned Kelimenin tamamen öğrenilip öğrenilmediği.
 */
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

/**
 * UI tarafında kelime listelerinde (RecyclerView) gösterilecek sadeleştirilmiş kelime modeli.
 * 
 * @property id Kelimenin benzersiz kimliği (String formatında).
 * @property text Ekranda gösterilecek kelime metni (İngilizce - Türkçe).
 * @property progress Kelimenin öğrenme ilerlemesi.
 */
data class WordItem(
    val id: String,
    val text: String,
    val progress: Int
)
//endregion
