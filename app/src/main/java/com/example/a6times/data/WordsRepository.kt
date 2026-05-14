package com.example.a6times.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class WordsRepository {
    //region Firebase kimlik doğrulaması ve Database referansı
    private val auth = FirebaseAuth.getInstance()
    private val databaseRef = FirebaseDatabase.getInstance(
        "https://six-times-228d1-default-rtdb.europe-west1.firebasedatabase.app"
    ).reference
    //endregion

    //region kelime ekleme fonksiyonu
    fun addWord(word: Words, samples: List<String>, onComplete: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(false, "Kullanıcı girişi yapılmamış.")
            return
        }

        // Sayacın tutulacağı referans (Kullanıcıya özel)
        val counterRef = databaseRef.child("Counters").child(userId)
        
        // Kelimelerin kaydedileceği referans (Kullanıcıya özel)
        val userWordsRef = databaseRef.child("Words").child(userId)

        // 1. Transaction başlatıyoruz (Aynı anda gelen istekleri sıraya sokar)
        counterRef.runTransaction(object : Transaction.Handler {
            //region Transaction işlemleri
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentWordId = mutableData.child("lastWordID").getValue(Int::class.java)
                val currentSampleId = mutableData.child("lastSampleID").getValue(Int::class.java)

                val nextWordId = idCounter(currentWordId)

                mutableData.child("lastWordID").value = nextWordId

                if (samples.isNotEmpty()) {
                    val startSampleId = idCounter(currentSampleId)
                    val endSampleId = startSampleId + samples.size - 1
                    mutableData.child("lastSampleID").value = endSampleId
                }

                return Transaction.success(mutableData)
            }
            //endregion
            //region Transaction başarı durumunda
            override fun onComplete(
                databaseError: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed && currentData != null) {
                    // 2. Transaction başarılı oldu, yeni ID'mizi aldık
                    val newWordId = currentData.child("lastWordID").getValue(Int::class.java) ?: 0
                    val endSampleId = currentData.child("lastSampleID").getValue(Int::class.java) ?: 0
                    val startSampleId = if (samples.isEmpty()) 0 else endSampleId - samples.size + 1

                    // 3. Objenin içindeki wordID'yi bu yeni ID ile güncelliyoruz
                    word.wordID = newWordId
                    
                    val samplesMap = mutableMapOf<String, WordSample>()
                    for ((index, sampleText) in samples.withIndex()) {
                        val sid = startSampleId + index
                        samplesMap[sid.toString()] = WordSample(sampleID = sid, sample = sampleText)
                    }

                    // 4. Kelimeyi kendi numarasıyla (0, 1, 2..) Words -> userId altına kaydediyoruz
                    userWordsRef.child(newWordId.toString()).setValue(word)
                        .addOnSuccessListener {// Eğer örnek cümleler varsa
                            if (samplesMap.isNotEmpty()) {
                                // 5. Örnek cümleleri doğrudan kelimenin altına (Words -> userId -> wordID -> samples) kaydediyoruz
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
                }
                else {
                    onComplete(false, databaseError?.message ?: "Sayaç işlemi başarısız oldu.")
                }

            }
            //endregion
        })
    }
    //endregion
    //region Listen To Words
    fun listenToWords(onDataChange: (List<Words>) -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid // Kullanıcının kimliğini al
        if (userId == null) {// Kullanıcı girişi yapılmamışsa hata geri döndür
            onError("Kullanıcı girişi yapılmamış.")
            return
        }

        val userWordsRef = databaseRef.child("Words").child(userId)
        // Words referansındaki verileri dinle
        userWordsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val wordsList = mutableListOf<Words>()// Listeyi sıfırla
                for (wordSnapshot in snapshot.children) {
                    val word = wordSnapshot.getValue(Words::class.java)                                                                      // yi Words nesnesine dönüştür
                    if (word != null) {
                        wordsList.add(word)// Listeye ekle
                    }
                }
                onDataChange(wordsList)// Listeyi geri döndür
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)// Hata durumunda hata mesajını geri döndür
            }
        })
    }
    //endregion

    fun idCounter(currentId : Int?): Int {
        return if (currentId == null) {
            0 // Veritabanı boşsa 0'dan başla
        } else {
            currentId + 1 // Doluysa 1 artır
        }
    }
}