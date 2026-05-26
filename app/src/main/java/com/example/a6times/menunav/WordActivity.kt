package com.example.a6times.menunav

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.a6times.R
import com.example.a6times.WordAdapter
import com.example.a6times.data.WordItem
import com.example.a6times.data.WordsRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Kullanıcının eklediği kelimeleri listeleyen ve yöneten (ekleme, düzenleme, silme) ana kelime ekranı.
 */
class WordActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var wordAdapter: WordAdapter
    private val wordList = mutableListOf<WordItem>()
    private val wordsRepository = WordsRepository()
    private var isEditModeActive = false

    private var currentDialogImageView: ImageView? = null
    private var selectedImageUri: Uri? = null

    /** Görsel seçme işlemini başlatan launcher */
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val copiedUri = copyImageToInternalStorage(uri)
            if (copiedUri != null) {
                selectedImageUri = copiedUri
                currentDialogImageView?.visibility = View.VISIBLE
                currentDialogImageView?.load(copiedUri) {
                    crossfade(true)
                }
                Toast.makeText(this, "Resim başarıyla seçildi.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Resim kopyalanamadı!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word)

        val fabAddWord = findViewById<FloatingActionButton>(R.id.fabAddWord)
        val fabEditWord = findViewById<FloatingActionButton>(R.id.fabEditWord)

        // Yeni kelime ekleme ekranına git
        fabAddWord.setOnClickListener {
            val intent = Intent(this, AddWordActivity::class.java)
            startActivity(intent)
        }

        val btnBack = findViewById<ImageButton>(R.id.WordBackButton)
        btnBack.setOnClickListener {
            if (isEditModeActive) {
                // Eğer düzenleme modundaysak önce o modu kapat
                toggleEditMode(fabAddWord, fabEditWord)
            } else {
                finish()
            }
        }

        // Düzenleme modunu aç/kapat
        fabEditWord.setOnClickListener {
            toggleEditMode(fabAddWord, fabEditWord)
        }

        recyclerView = findViewById(R.id.rvWords)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Adaptörü yeni işlevlerle (Düzenle ve Sil) başlat
        wordAdapter = WordAdapter(wordList, 
            onEditClick = { wordItem -> showEditDialog(wordItem) },
            onDeleteClick = { wordItem -> confirmAndDeleteWord(wordItem) }
        )
        recyclerView.adapter = wordAdapter

        // Kelimeleri Firebase'den çek
        fetchWordsDynamically()
    }

    /**
     * Liste üzerindeki düzenleme (edit/delete) modları arasında geçiş yapar.
     */
    private fun toggleEditMode(fabAddWord: FloatingActionButton, fabEditWord: FloatingActionButton) {
        isEditModeActive = !isEditModeActive
        wordAdapter.isEditMode = isEditModeActive
        wordAdapter.notifyDataSetChanged()

        if (isEditModeActive) {
            fabAddWord.hide()
            fabEditWord.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            Toast.makeText(this, "Düzenleme modu aktif. Kelimelerin yanındaki kaleme dokunun.", Toast.LENGTH_SHORT).show()
        } else {
            fabAddWord.show()
            fabEditWord.setImageResource(android.R.drawable.ic_menu_edit)
            Toast.makeText(this, "Normal moda dönüldü.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Firebase'deki kelimeleri gerçek zamanlı olarak dinler ve listeyi günceller.
     */
    private fun fetchWordsDynamically() {
        wordsRepository.listenToWords(
            onDataChange = { words ->
                wordList.clear()
                for (word in words) {
                    val displayText = "${word.engWordName} - ${word.turWordName}"
                    wordList.add(WordItem(word.wordID.toString(), displayText, word.progress))
                }
                wordAdapter.notifyDataSetChanged()
            },
            onError = { errorMessage ->
                Toast.makeText(this, "Hata: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun confirmAndDeleteWord(wordItem: WordItem) {
        AlertDialog.Builder(this)
            .setTitle("Kelimeyi Sileyim mi?")
            .setMessage("Bu kelimeyi silmek istediğine emin misin?")
            .setPositiveButton("Evet") { _, _ ->
                wordsRepository.deleteWordFromFirebase(
                    wordId = wordItem.id,
                    onSuccess = {
                        val index = wordList.indexOfFirst { it.id == wordItem.id }
                        if (index != -1) {
                            wordList.removeAt(index)
                            wordAdapter.notifyItemRemoved(index)
                            Toast.makeText(this, "Kelime kalıcı olarak silindi", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { errorMessage ->
                        Toast.makeText(this, "Hata: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Hayır") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showEditDialog(wordItem: WordItem) {
        selectedImageUri = null
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Kelimeyi Düzenle")

        val layoutView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_word, null)

        val etEng = layoutView.findViewById<EditText>(R.id.etEng)
        val etTur = layoutView.findViewById<EditText>(R.id.etTur)
        val etCategory = layoutView.findViewById<EditText>(R.id.etCategory)
        val etSamples = layoutView.findViewById<EditText>(R.id.etSamples)
        val ivDialogImage = layoutView.findViewById<ImageView>(R.id.ivDialogImage)
        val btnSelectImg = layoutView.findViewById<Button>(R.id.btnSelectImg)

        currentDialogImageView = ivDialogImage

        btnSelectImg.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Mevcut verileri çek ve alanlara yerleştir
        wordsRepository.getWordDetails(wordItem.id) { word ->
            if (word != null) {
                etEng.setText(word.engWordName)
                etTur.setText(word.turWordName)
                etCategory.setText(word.category)

                if (word.picture.isNotEmpty()) {
                    selectedImageUri = Uri.parse(word.picture)
                    ivDialogImage.visibility = View.VISIBLE
                    ivDialogImage.load(selectedImageUri) {
                        crossfade(true)
                        placeholder(android.R.color.transparent)
                        error(android.R.color.transparent)
                    }
                }

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    FirebaseDatabase.getInstance(com.example.a6times.utils.Constants.FIREBASE_DATABASE_URL)
                        .reference.child("Words").child(userId).child(wordItem.id).child("samples")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val samplesBuilder = StringBuilder()
                                    for (sampleSnapshot in snapshot.children) {
                                        val sampleText = sampleSnapshot.child("sample").getValue(String::class.java)
                                            ?: sampleSnapshot.getValue(String::class.java)
                                        if (sampleText != null) {
                                            samplesBuilder.append(sampleText).append("\n")
                                        }
                                    }
                                    etSamples.setText(samplesBuilder.toString().trim())
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }
        }

        builder.setView(layoutView)

        // Güncelleme işlemi
        builder.setPositiveButton("Güncelle") { _, _ ->
            val newEng = etEng.text.toString().trim()
            val newTur = etTur.text.toString().trim()
            val newCategory = etCategory.text.toString().trim()
            val newSamples = etSamples.text.toString().trim()
            val newPicturePath = selectedImageUri?.toString() ?: ""

            if (newEng.isNotEmpty() && newTur.isNotEmpty() && newCategory.isNotEmpty() && newSamples.isNotEmpty()) {
                val samplesList = newSamples.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
                val samplesMap = mutableMapOf<String, Any>()

                var startId = System.currentTimeMillis().toInt()
                for ((index, sampleText) in samplesList.withIndex()) {
                    val sid = startId + index
                    samplesMap[sid.toString()] = mapOf("sampleID" to sid, "sample" to sampleText)
                }

                wordsRepository.updateWordInFirebase(
                    wordId = wordItem.id,
                    newEngName = newEng,
                    newTurName = newTur,
                    newCategory = newCategory,
                    newPicturePath = newPicturePath,
                    newSamplesMap = samplesMap,
                    onSuccess = {
                        Toast.makeText(this, "Kelime başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(this, "Hata: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, "Alanlar boş bırakılamaz!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("İptal", null)
        builder.create().show()
    }

    /**
     * Seçilen görseli uygulamanın dahili depolama alanına kopyalar.
     */
    private fun copyImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = "word_image_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(filesDir, fileName)
            val outputStream = java.io.FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }

    override fun onBackPressed() {
        if (isEditModeActive) {
            toggleEditMode(findViewById(R.id.fabAddWord), findViewById(R.id.fabEditWord))
        } else {
            super.onBackPressed()
        }
    }
}