package com.example.a6times.menunav

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.a6times.R
import com.example.a6times.WordActivity
import com.example.a6times.data.Words
import com.example.a6times.data.WordsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddWordActivity : AppCompatActivity() {
    private val wordRepo = WordsRepository()
    private var selectedImageUri: Uri? = null
    private var editWordId: String? = null

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

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val copiedUri = copyImageToInternalStorage(uri)
            if (copiedUri != null) {
                selectedImageUri = copiedUri
                Toast.makeText(this, "Resim seçildi!", Toast.LENGTH_SHORT).show()
                val ivSelectedImage = findViewById<ImageView>(R.id.ivSelectedImage)
                ivSelectedImage.visibility = ImageView.VISIBLE
                ivSelectedImage.load(selectedImageUri) {
                    crossfade(true)
                    placeholder(android.R.color.transparent)
                    error(android.R.color.transparent)
                }
            } else {
                Toast.makeText(this, "Resim kopyalanamadı!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Resim seçilmedi!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_word)

        val etEngWord = findViewById<EditText>(R.id.etEngWord)
        val etTurWord = findViewById<EditText>(R.id.etTurWord)
        val etCategory = findViewById<EditText>(R.id.etCategory)
        val etSamples = findViewById<EditText>(R.id.etSamples)
        val ivSelectedImage = findViewById<ImageView>(R.id.ivSelectedImage)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val btnSave = findViewById<Button>(R.id.btnSaveWord)

        editWordId = intent.getStringExtra("EDIT_WORD_ID")

        if (editWordId != null) {
            btnSave.text = "Güncelle"
            wordRepo.getWordDetails(editWordId!!) { word ->
                if (word != null) {
                    etEngWord.setText(word.engWordName)
                    etTurWord.setText(word.turWordName)
                    etCategory.setText(word.category)

                    if (word.picture.isNotEmpty()) {
                        selectedImageUri = Uri.parse(word.picture)
                        ivSelectedImage.visibility = ImageView.VISIBLE
                            ivSelectedImage.load(selectedImageUri) {
                                crossfade(true)
                                placeholder(android.R.color.transparent)
                                error(android.R.color.transparent)
                            }
                    }

                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        FirebaseDatabase.getInstance("https://six-times-228d1-default-rtdb.europe-west1.firebasedatabase.app")
                            .reference.child("Words").child(userId).child(editWordId!!).child("samples")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val samplesBuilder = StringBuilder()
                                        for (sampleSnapshot in snapshot.children) {
                                            val sampleText = sampleSnapshot.child("sample").getValue(String::class.java)
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
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val engWordInput = etEngWord.text.toString().trim()
            val turWordInput = etTurWord.text.toString().trim()
            val categoryInput = etCategory.text.toString().trim()
            val samplesInput = etSamples.text.toString().trim()
            val picturePath = selectedImageUri?.toString() ?: ""

            if (engWordInput.isEmpty() || turWordInput.isEmpty() || categoryInput.isEmpty() || samplesInput.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editWordId != null) {
                val samplesList = samplesInput.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
                val samplesMap = mutableMapOf<String, Any>()
                var startId = System.currentTimeMillis().toInt()
                for ((index, sampleText) in samplesList.withIndex()) {
                    val sid = startId + index
                    samplesMap[sid.toString()] = mapOf("sampleID" to sid, "sample" to sampleText)
                }

                wordRepo.updateWordInFirebase(
                    wordId = editWordId!!,
                    newEngName = engWordInput,
                    newTurName = turWordInput,
                    newCategory = categoryInput,
                    newPicturePath = picturePath,
                    newSamplesMap = samplesMap,
                    onSuccess = {
                        showSuccessDialog(etEngWord, etTurWord, etCategory, etSamples)
                    },
                    onError = { message ->
                        Toast.makeText(this, "Hata: $message", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                val newWord = Words(
                    engWordName = engWordInput,
                    turWordName = turWordInput,
                    category = categoryInput,
                    picture = picturePath
                )

                val samplesList = samplesInput.split('\n')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                wordRepo.addWord(newWord, samplesList) { isSuccess, message ->
                    if (isSuccess) {
                        showSuccessDialog(etEngWord, etTurWord, etCategory, etSamples)
                    } else {
                        Toast.makeText(this, "Hata: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showSuccessDialog(
        etEngWord: EditText,
        etTurWord: EditText,
        etCategory: EditText,
        etSamples: EditText
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Başarılı")

        if (editWordId != null) {
            builder.setMessage("Kelime başarıyla güncellendi.")
            builder.setPositiveButton("Kelimelerim Sayfasına Dön") { _, _ ->
                val intent = Intent(this, WordActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        } else {
            builder.setMessage("Kelime kaydedildi.")
            builder.setPositiveButton("Ana Sayfaya Dön") { _, _ ->
                val intent = Intent(this, WordActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton("Yeni Kelime Ekle") { dialog, _ ->
                dialog.dismiss()
                etEngWord.text.clear()
                etTurWord.text.clear()
                etCategory.text.clear()
                etSamples.text.clear()
                findViewById<ImageView>(R.id.ivSelectedImage).visibility = ImageView.GONE
            }
        }
        builder.create().show()
    }
}