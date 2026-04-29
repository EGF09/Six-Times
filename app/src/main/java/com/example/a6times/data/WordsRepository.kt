package com.example.a6times.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

class WordsRepository {
    // Kelimelerin kaydedileceği referans
    private val database = FirebaseDatabase.getInstance("https://six-times-228d1-default-rtdb.europe-west1.firebasedatabase.app").reference.child("Words")

    // Sayacın tutulacağı referans
    private val counterRef = FirebaseDatabase.getInstance("https://six-times-228d1-default-rtdb.europe-west1.firebasedatabase.app").reference.child("Counters").child("lastWordID")

    fun addWord(word: Words, onComplete: (Boolean, String?) -> Unit) {

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

                    // 4. Kelimeyi kendi numarasıyla (0, 1, 2..) Words klasörüne kaydediyoruz
                    database.child(newId.toString()).setValue(word)
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