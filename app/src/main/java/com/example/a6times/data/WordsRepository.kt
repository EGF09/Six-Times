package com.example.a6times.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class WordsRepository {
    private val auth = FirebaseAuth.getInstance()
    private val databaseRef = FirebaseDatabase.getInstance(
        "https://six-times-228d1-default-rtdb.europe-west1.firebasedatabase.app"
    ).reference

    fun addWord(word: Words, samples: List<String>, onComplete: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(false, "Kullanıcı girişi yapılmamış.")
            return
        }

        val counterRef = databaseRef.child("Counters").child(userId)
        val userWordsRef = databaseRef.child("Words").child(userId)

        counterRef.runTransaction(object : Transaction.Handler {
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

                    val samplesMap = mutableMapOf<String, WordSample>()
                    for ((index, sampleText) in samples.withIndex()) {
                        val sid = startSampleId + index
                        samplesMap[sid.toString()] = WordSample(sampleID = sid, sample = sampleText)
                    }

                    userWordsRef.child(newWordId.toString()).setValue(word)
                        .addOnSuccessListener {
                            if (samplesMap.isNotEmpty()) {
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

    fun idCounter(currentId: Int?): Int {
        return if (currentId == null) 0 else currentId + 1
    }

    fun getExamReadyWords(allWords: List<Words>): List<Words> {
        val currentTime = System.currentTimeMillis()
        val readyWords = mutableListOf<Words>()

        for (word in allWords) {
            if (word.isLearned) continue

            if (word.isActive) {
                readyWords.add(word)
            } else {
                if (word.nextReviewAt in 1..currentTime) {
                    word.isActive = true
                    updateWordStatus(word.wordID, isActive = true)
                    readyWords.add(word)
                }
            }
        }
        return readyWords
    }

    private fun updateWordStatus(wordId: Int, isActive: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        databaseRef.child("Words").child(userId).child(wordId.toString()).child("active").setValue(isActive)
    }

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
            word.progress -= 1
            if (word.progress < 0) word.progress = 0
            word.isActive = true
        }

        val wordRef = databaseRef.child("Words").child(userId).child(word.wordID.toString())
        wordRef.setValue(word)
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener { onComplete?.invoke(false) }
    }

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