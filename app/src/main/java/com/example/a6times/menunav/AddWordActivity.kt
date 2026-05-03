package com.example.a6times.menunav

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.a6times.R
import com.example.a6times.data.Words
import com.example.a6times.data.WordsRepository
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.widget.ImageView

class AddWordActivity : AppCompatActivity() {
    private val wordRepo = WordsRepository()
    private var selectedImageUri: Uri? = null

    //region ImagePicker function
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(this, "Resim seçildi!", Toast.LENGTH_SHORT).show()
            findViewById<ImageView>(R.id.ivSelectedImage).visibility = ImageView.VISIBLE
            findViewById<ImageView>(R.id.ivSelectedImage).setImageURI(selectedImageUri)
        } else {
            Toast.makeText(this, "Resim seçilmedi!", Toast.LENGTH_SHORT).show()
        }
    }
    //endregion


    //region Activity Functions
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_word)

        val etEngWord = findViewById<EditText>(R.id.etEngWord)
        val etTurWord = findViewById<EditText>(R.id.etTurWord)
        val etCategory = findViewById<EditText>(R.id.etCategory)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val btnSave = findViewById<Button>(R.id.btnSaveWord)

        //region Back Btn Functions
        btnBack.setOnClickListener {
            finish()
        }
        //endregion

        //region ImagePicker Button
        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        //endregion

        //region Save Btn Functions
        btnSave.setOnClickListener {

            val engWordInput = etEngWord.text.toString().trim()
            val turWordInput = etTurWord.text.toString().trim()
            val categoryInput = etCategory.text.toString().trim()
            val picturePath = selectedImageUri?.toString() ?: ""

            if (engWordInput.isEmpty()|| turWordInput.isEmpty()) {//Blank check
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newWord = Words(//New word object creation
                engWordName = engWordInput,
                turWordName = turWordInput,
                category = categoryInput,
                picture = picturePath
            )

            wordRepo.addWord(newWord) { isSuccess, message ->//wordRepo addWord function
                if (isSuccess) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Hata: $message", Toast.LENGTH_SHORT).show()
                }
            }
            showSuccessDialog(etEngWord, etTurWord, etCategory)//Success Dialog
        }
        //endregion
    }
    //endregion

    //region Success Dialog
    private fun showSuccessDialog(etEngWord: EditText,
                                  etTurWord: EditText,
                                  etCategory: EditText) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Başarılı")
        builder.setMessage("Kelime kaydedildi.")

        //region Main Menu Button
        builder.setPositiveButton("Ana Sayfaya Dön") { _, _ ->//Back to home
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        //endregion

        //region New Word Button
        builder.setNegativeButton("Yeni Kelime Ekle") { dialog, _ ->//Clear all fields
            dialog.dismiss()
            etEngWord.text.clear()
            etTurWord.text.clear()
            etCategory.text.clear()
            findViewById<ImageView>(R.id.ivSelectedImage).visibility = ImageView.GONE//ImagePicker Clearing

        }
        //endregion
        builder.create().show()//Dialog Show
    }//endregion
}