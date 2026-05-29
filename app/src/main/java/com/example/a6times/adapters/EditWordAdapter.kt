package com.example.a6times.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.a6times.R
import com.example.a6times.data.WordItem
import com.example.a6times.data.WordsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Kelime düzenleme ekranı için RecyclerView adaptörü.
 * Kelimelerin listelenmesini ve düzenleme diyalogunun açılmasını sağlar.
 * 
 * @property wordList Düzenlenecek kelimelerin listesi.
 */
class EditWordAdapter(private val wordList: MutableList<WordItem>) : RecyclerView.Adapter<EditWordAdapter.EditViewHolder>() {

    private var currentDialogImageView: ImageView? = null
    private var selectedImageUri: android.net.Uri? = null

    /**
     * Kelime öğesinin görünüm bileşenlerini tutan ViewHolder sınıfı.
     */
    class EditViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWordText: TextView = view.findViewById(R.id.tvWordText)
        val tvWordPercent: TextView = view.findViewById(R.id.tvWordPercent)
        val pbWordProgress: ProgressBar = view.findViewById(R.id.pbWordProgress)
        val btnDeleteRow: ImageButton = view.findViewById(R.id.btnDeleteRow) // Kalem ikonu olarak kullanılıyor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
        return EditViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditViewHolder, position: Int) {
        val currentWord = wordList[position]
        holder.tvWordText.text = currentWord.text

        // İlerleme hesaplaması ve görselleştirilmesi
        val res = currentWord.progress * 16.7
        holder.tvWordPercent.text = "%" + "%.0f".format(res)
        holder.pbWordProgress.progress = (currentWord.progress * 17).toInt()

        // İkonu düzenleme (kalem) ikonuna çevir
        holder.btnDeleteRow.setImageResource(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI.let {
            android.R.drawable.ic_menu_edit
        })

        // Düzenleme butonuna tıklama işlemi
        holder.btnDeleteRow.setOnClickListener {
            val context = holder.itemView.context
            val wordsRepository = WordsRepository()

            selectedImageUri = null

            // Düzenleme Diyalogu Tasarımı
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Kelimeyi Düzenle")

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(60, 40, 60, 20)
            }

            // Girdi alanlarının tanımlanması
            val tvEngLabel = TextView(context).apply { text = "İngilizce Kelime"; setTypeface(null, android.graphics.Typeface.BOLD) }
            val etEng = EditText(context).apply { hint = "Örn: Apple" }

            val tvTurLabel = TextView(context).apply { text = "Türkçe Karşılığı"; setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, 20, 0, 0) }
            val etTur = EditText(context).apply { hint = "Örn: Elma" }

            val tvCategoryLabel = TextView(context).apply { text = "Kategori"; setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, 20, 0, 0) }
            val etCategory = EditText(context).apply { hint = "Örn: Fruit" }

            val tvSamplesLabel = TextView(context).apply { text = "Örnek Cümleler"; setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, 20, 0, 0) }
            val etSamples = EditText(context).apply { hint = "Her cümleyi yeni satıra yazın" }

            val tvImageLabel = TextView(context).apply { text = "Kelime Görseli"; setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, 20, 0, 0) }
            val ivDialogImage = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(200, 200).apply { setMargins(0, 10, 0, 10) }
                visibility = View.GONE
            }
            currentDialogImageView = ivDialogImage

            val btnSelectImg = android.widget.Button(context).apply {
                text = "Görsel Seç / Değiştir"
                setOnClickListener {
                    val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
                    (context as? android.app.Activity)?.startActivityForResult(intent, 998)
                }
            }

            // Bileşenleri yerleşime ekle
            layout.addView(tvEngLabel)
            layout.addView(etEng)
            layout.addView(tvTurLabel)
            layout.addView(etTur)
            layout.addView(tvCategoryLabel)
            layout.addView(etCategory)
            layout.addView(tvSamplesLabel)
            layout.addView(etSamples)
            layout.addView(tvImageLabel)
            layout.addView(ivDialogImage)
            layout.addView(btnSelectImg)

            // Mevcut kelime verilerini getir ve alanları doldur
            wordsRepository.getWordDetails(currentWord.id) { word ->
                if (word != null) {
                    etEng.setText(word.engWordName)
                    etTur.setText(word.turWordName)
                    etCategory.setText(word.category)

                    if (word.picture.isNotEmpty()) {
                        selectedImageUri = android.net.Uri.parse(word.picture)
                        ivDialogImage.visibility = View.VISIBLE
                        ivDialogImage.load(word.picture) {
                            crossfade(true)
                            placeholder(android.R.color.transparent)
                            error(android.R.color.transparent)
                        }
                    }

                    // Örnek cümleleri Firebase'den çek
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        FirebaseDatabase.getInstance("https://six-times-228d1-default-rtdb.europe-west1.firebasedatabase.app")
                            .reference.child("Words").child(userId).child(currentWord.id).child("samples")
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

            builder.setView(layout)

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

                    // Yeni örnek cümle haritasını oluştur
                    var startId = System.currentTimeMillis().toInt()
                    for ((index, sampleText) in samplesList.withIndex()) {
                        val sid = startId + index
                        samplesMap[sid.toString()] = mapOf("sampleID" to sid, "sample" to sampleText)
                    }

                    // Veritabanını güncelle
                    wordsRepository.updateWordInFirebase(
                        wordId = currentWord.id,
                        newEngName = newEng,
                        newTurName = newTur,
                        newCategory = newCategory,
                        newPicturePath = newPicturePath,
                        newSamplesMap = samplesMap,
                        onSuccess = {
                            holder.tvWordText.text = "$newEng - $newTur"
                            Toast.makeText(context, "Kelime başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, "Hata: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Alanlar boş bırakılamaz!", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("İptal", null)
            builder.create().show()
        }
    }

    override fun getItemCount(): Int = wordList.size

    /**
     * Seçilen görseli uygulamanın dahili depolama alanına kopyalar.
     * 
     * @param context Uygulama bağlamı.
     * @param uri Seçilen görselin URI değeri.
     * @return Kopyalanan dosyanın yeni URI değeri.
     */
    private fun copyImageToInternalStorage(context: android.content.Context, uri: android.net.Uri): android.net.Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "word_image_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(context.filesDir, fileName)
            val outputStream = java.io.FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            android.net.Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Görsel seçme işleminden dönen sonucu işler.
     * 
     * @param requestCode İstek kodu.
     * @param resultCode Sonuç kodu.
     * @param data Dönen veri.
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        if (requestCode == 998 && resultCode == android.app.Activity.RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                val context = currentDialogImageView?.context
                if (context != null) {
                    val copiedUri = copyImageToInternalStorage(context, uri)
                    if (copiedUri != null) {
                        selectedImageUri = copiedUri
                        currentDialogImageView?.visibility = View.VISIBLE
                        currentDialogImageView?.load(copiedUri) {
                            crossfade(true)
                        }
                        Toast.makeText(context, "Resim başarıyla seçildi.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Resim kopyalanamadı!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
