package com.example.a6times.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

class WordsRepository {
    private val auth = FirebaseAuth.getInstance()
    private val databaseRef = FirebaseDatabase.getInstance(
        "https://six-times-228d1-default-rtdb.europe-west1.firebasedatabase.app"
    ).reference

    fun addWord(word: Words, onComplete: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(false, "Kullanıcı girişi yapılmamış.")
            return
        }

        // Sayacın tutulacağı referans (Kullanıcıya özel)
        val counterRef = databaseRef.child("Counters").child(userId).child("lastWordID")
        
        // Kelimelerin kaydedileceği referans (Kullanıcıya özel)
        val userWordsRef = databaseRef.child("Words").child(userId)

        // 1. Transaction başlatıyoruz (Aynı anda gelen istekleri sıraya sokar)
        counterRef.runTransaction(object : Transaction.Handler {

            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentId = mutableData.getValue(Int::class.java)

                val nextId = if (currentId == null) {
                    0 // Veritabanı boşsa 0'dan başla
                } else {
                    currentId + 1 // Doluysa 1 artır
                }

                mutableData.value = nextId
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                databaseError: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed && currentData != null) {
                    // 2. Transaction başarılı oldu, yeni ID'mizi aldık
                    val newId = currentData.getValue(Int::class.java) ?: 0

                    // 3. Objenin içindeki wordID'yi bu yeni ID ile güncelliyoruz
                    word.wordID = newId

                    // 4. Kelimeyi kendi numarasıyla (0, 1, 2..) Words -> userId altına kaydediyoruz
                    userWordsRef.child(newId.toString()).setValue(word)
                        .addOnSuccessListener {
                            onComplete(true, "Kelime eklendi. Yeni ID: $newId")
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
}