package com.example.a6times.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

/**
 * Kelime verilerinin Firebase Realtime Database üzerinden yönetilmesini sağlayan depo sınıfı.
 * Kelime ekleme, silme, güncelleme ve öğrenme ilerlemesi takibi işlemlerini gerçekleştirir.
 */
class WordsRepository {
    private val auth = FirebaseAuth.getInstance()
    private val databaseRef = FirebaseDatabase.getInstance(
        "https://six-times-228d1-default-rtdb.europe-west1.firebasedatabase.app"
    ).reference

    /**
     * Yeni bir kelimeyi ve ilişkili örnek cümleleri veritabanına ekler.
     * İşlem sırasında benzersiz ID ataması için Firebase Transaction kullanır.
     * 
     * @param word Eklenecek kelime nesnesi.
     * @param samples Kelimeyle ilişkili örnek cümleler listesi.
     * @param onComplete İşlem tamamlandığında çağrılacak geri bildirim fonksiyonu.
     */
    fun addWord(word: Words, samples: List<String>, onComplete: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(false, "Kullanıcı girişi yapılmamış.")
            return
        }

        val counterRef = databaseRef.child("Counters").child(userId)
        val userWordsRef = databaseRef.child("Words").child(userId)

        // Benzersiz ID'ler için sayaç güncelleme işlemi (Transaction)
        counterRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentWordId = mutableData.child("lastWordID").getValue(Int::class.java)
                val currentSampleId = mutableData.child("lastSampleID").getValue(Int::class.java)

                val nextWordId = idCounter(currentWordId)

                mutableData.child("lastWordID").value = nextWordId

                // Örnek cümleler varsa onlara da ID ata
                if (samples.isNotEmpty()) {
                    val startSampleId = idCounter(currentSampleId)
                    val endSampleId = startSampleId + samples.size - 1
                    mutableData.child("lastSampleID").value = endSampleId
                }

                return Transaction.success(mutableData)
            }

            override fun onComplete(
                databaseError: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed && currentData != null) {
                    val newWordId = currentData.child("lastWordID").getValue(Int::class.java) ?: 0
                    val endSampleId = currentData.child("lastSampleID").getValue(Int::class.java) ?: 0
                    val startSampleId = if (samples.isEmpty()) 0 else endSampleId - samples.size + 1

                    word.wordID = newWordId

                    // Örnek cümleleri haritalandır
                    val samplesMap = mutableMapOf<String, WordSample>()
                    for ((index, sampleText) in samples.withIndex()) {
                        val sid = startSampleId + index
                        samplesMap[sid.toString()] = WordSample(sampleID = sid, sample = sampleText)
                    }

                    // Kelime bilgilerini kaydet
                    userWordsRef.child(newWordId.toString()).setValue(word)
                        .addOnSuccessListener {
                            if (samplesMap.isNotEmpty()) {
                                // Örnek cümleleri kaydet
                                userWordsRef.child(newWordId.toString()).child("samples").setValue(samplesMap)
                                    .addOnSuccessListener {
                                        onComplete(true, "Kelime ve örnek cümleler eklendi. Yeni ID: $newWordId")
                                    }
                                    .addOnFailureListener { error ->
                                        onComplete(false, "Örnek cümleler eklenemedi: ${error.message}")
                                    }
                            } else {
                                onComplete(true, "Kelime eklendi (örnek cümle yok). Yeni ID: $newWordId")
                            }
                        }
                        .addOnFailureListener { error ->
                            onComplete(false, error.message)
                        }
                } else {
                    onComplete(false, databaseError?.message ?: "Sayaç işlemi başarısız oldu.")
                }
            }
        })
    }

    /**
     * Kullanıcının tüm kelimelerini gerçek zamanlı olarak dinler.
     * 
     * @param onDataChange Veri değiştiğinde çağrılacak fonksiyon.
     * @param onError Hata durumunda çağrılacak fonksiyon.
     */
    fun listenToWords(onDataChange: (List<Words>) -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Kullanıcı girişi yapılmamış.")
            return
        }

        val userWordsRef = databaseRef.child("Words").child(userId)
        userWordsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val wordsList = mutableListOf<Words>()
                for (wordSnapshot in snapshot.children) {
                    val word = wordSnapshot.getValue(Words::class.java)
                    if (word != null) {
                        wordsList.add(word)
                    }
                }
                onDataChange(wordsList)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        })
    }

    /**
     * Kullanıcının kelimelerini bir kez çeker.
     * 
     * @param onDataChange Veri alındığında çağrılacak fonksiyon.
     * @param onError Hata durumunda çağrılacak fonksiyon.
     */
    fun getWordsOnce(onDataChange: (List<Words>) -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Kullanıcı girişi yapılmamış.")
            return
        }

        val userWordsRef = databaseRef.child("Words").child(userId)
        userWordsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val wordsList = mutableListOf<Words>()
                for (wordSnapshot in snapshot.children) {
                    val word = wordSnapshot.getValue(Words::class.java)
                    if (word != null) {
                        wordsList.add(word)
                    }
                }
                onDataChange(wordsList)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        })
    }

    /**
     * Mevcut ID değerini bir artırır.
     * 
     * @param currentId Mevcut ID değeri.
     * @return Yeni ID değeri.
     */
    fun idCounter(currentId: Int?): Int {
        return if (currentId == null) 0 else currentId + 1
    }

    /**
     * Sınav için hazır olan kelimeleri filtreler.
     * Henüz öğrenilmemiş ve tekrar zamanı gelmiş kelimeleri seçer.
     * 
     * @param allWords Tüm kelimeler listesi.
     * @return Sınava hazır kelimeler listesi.
     */
    fun getExamReadyWords(allWords: List<Words>): List<Words> {
        val currentTime = System.currentTimeMillis()
        val readyWords = mutableListOf<Words>()

        for (word in allWords) {
            if (word.isLearned) continue

            if (word.isActive) {
                readyWords.add(word)
            } else {
                // Tekrar zamanı gelmiş mi kontrol et
                if (word.nextReviewAt in 1..currentTime) {
                    word.isActive = true
                    updateWordStatus(word.wordID, isActive = true)
                    readyWords.add(word)
                }
            }
        }
        return readyWords
    }

    /**
     * Kelimenin aktiflik durumunu veritabanında günceller.
     * 
     * @param wordId Kelime ID.
     * @param isActive Aktiflik durumu.
     */
    private fun updateWordStatus(wordId: Int, isActive: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        databaseRef.child("Words").child(userId).child(wordId.toString()).child("active").setValue(isActive)
    }

    /**
     * Kelimenin öğrenme ilerlemesini sınav sonucuna göre günceller.
     * Aralıklı tekrar (Spaced Repetition) mantığına göre bir sonraki tekrar zamanını hesaplar.
     * 
     * @param word Güncellenecek kelime nesnesi.
     * @param isCorrect Sınavda doğru cevaplanıp cevaplanmadığı.
     * @param onComplete İşlem bittiğinde çağrılacak geri bildirim fonksiyonu.
     */
    fun updateWordProgress(word: Words, isCorrect: Boolean, onComplete: ((Boolean) -> Unit)? = null) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete?.invoke(false)
            return
        }

        val currentTime = System.currentTimeMillis()
        word.lastReviewedAt = currentTime

        if (isCorrect) {
            word.progress += 1
            word.isActive = false

            // İlerleme aşamasına göre bir sonraki tekrar süresini belirle
            val oneDayMs = 86400000L
            when (word.progress) {
                1 -> word.nextReviewAt = currentTime + oneDayMs
                2 -> word.nextReviewAt = currentTime + 7L * oneDayMs
                3 -> word.nextReviewAt = currentTime + 30L * oneDayMs
                4 -> word.nextReviewAt = currentTime + 90L * oneDayMs
                5 -> word.nextReviewAt = currentTime + 180L * oneDayMs
                6 -> {
                    word.nextReviewAt = currentTime + 365L * oneDayMs
                    word.isLearned = true
                }
                else -> {
                    if (word.progress > 6) {
                        word.progress = 6
                        word.isLearned = true
                    }
                }
            }
        } else {
            // Yanlış cevapta ilerlemeyi düşür ve kelimeyi hemen aktif yap
            word.progress -= 1
            if (word.progress < 0) word.progress = 0
            word.isActive = true
        }

        val wordRef = databaseRef.child("Words").child(userId).child(word.wordID.toString())
        wordRef.setValue(word)
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener { onComplete?.invoke(false) }
    }

    /**
     * Belirtilen kelimeyi veritabanından siler.
     * 
     * @param wordId Silinecek kelimenin ID'si.
     * @param onSuccess Başarılı olduğunda çalışacak fonksiyon.
     * @param onError Hata durumunda çalışacak fonksiyon.
     */
    fun deleteWordFromFirebase(wordId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Kullanıcı girişi yapılmamış.")
            return
        }

        databaseRef.child("Words").child(userId).child(wordId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError(exception.message ?: "Silme başarısız.") }
    }

    /**
     * Mevcut bir kelimenin bilgilerini günceller.
     * 
     * @param wordId Güncellenecek kelime ID'si.
     * @param newEngName Yeni İngilizce karşılık.
     * @param newTurName Yeni Türkçe karşılık.
     * @param newCategory Yeni kategori.
     * @param newPicturePath Yeni resim yolu.
     * @param newSamplesMap Yeni örnek cümleler haritası.
     * @param onSuccess Başarılı olduğunda çalışacak fonksiyon.
     * @param onError Hata durumunda çalışacak fonksiyon.
     */
    fun updateWordInFirebase(
        wordId: String,
        newEngName: String,
        newTurName: String,
        newCategory: String? = null,
        newPicturePath: String? = null,
        newSamplesMap: Map<String, Any>? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Kullanıcı girişi yapılmamış.")
            return
        }

        val wordIdInt = wordId.toIntOrNull()
        if (wordIdInt == null) {
            onError("Geçersiz kelime ID formatı.")
            return
        }

        val wordRef = databaseRef.child("Words").child(userId).child(wordIdInt.toString())

        val updates = mutableMapOf<String, Any>(
            "engWordName" to newEngName,
            "turWordName" to newTurName
        )
        
        if (newCategory != null) {
            updates["category"] = newCategory
        }
        if (newPicturePath != null) {
            updates["picture"] = newPicturePath
        }
        if (newSamplesMap != null) {
            updates["samples"] = newSamplesMap
        }

        wordRef.updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError(exception.message ?: "Güncelleme başarısız.") }
    }

    /**
     * Belirli bir kelimenin detaylarını getirir.
     * 
     * @param wordId Kelime ID'si.
     * @param onComplete İşlem bittiğinde kelime nesnesini dönen fonksiyon.
     */
    fun getWordDetails(wordId: String, onComplete: (Words?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onComplete(null)

        databaseRef.child("Words").child(userId).child(wordId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val word = snapshot.getValue(Words::class.java)
                onComplete(word)
            }
            override fun onCancelled(error: DatabaseError) {
                onComplete(null)
            }
        })
    }

    /**
     * Belirli bir kelimeye ait örnek cümleleri getirir.
     * 
     * @param wordId Kelime ID'si.
     * @param onComplete İşlem bittiğinde örnek cümle listesini dönen fonksiyon.
     */
    fun getWordSamples(wordId: Int, onComplete: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(emptyList())
            return
        }

        databaseRef.child("Words").child(userId).child(wordId.toString()).child("samples")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val samples = mutableListOf<String>()
                    for (child in snapshot.children) {
                        val text = child.child("sample").getValue(String::class.java)
                            ?: child.getValue(String::class.java)
                        if (text != null) {
                            samples.add(text)
                        }
                    }
                    onComplete(samples)
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(emptyList())
                }
            })
    }
}
