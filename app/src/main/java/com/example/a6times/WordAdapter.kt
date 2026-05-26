package com.example.a6times

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.example.a6times.data.WordItem
import com.example.a6times.data.WordsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Ana kelime listesi için RecyclerView adaptörü.
 * Moduna göre (Düzenleme veya Silme) farklı işlevler sunar.
 * 
 * @property wordList Görüntülenecek kelime öğelerinin listesi.
 */
class WordAdapter(private val wordList: MutableList<WordItem>) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    /** Düzenleme modu aktif mi? (true: düzenleme, false: silme) */
    var isEditMode: Boolean = false
    private var currentDialogImageView: ImageView? = null
    private var selectedImageUri: Uri? = null

    /**
     * Kelime öğesinin görünüm bileşenlerini tutan ViewHolder sınıfı.
     */
    class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWordText: TextView = view.findViewById(R.id.tvWordText)
        val tvWordPercent: TextView = view.findViewById(R.id.tvWordPercent)
        val pbWordProgress: ProgressBar = view.findViewById(R.id.pbWordProgress)
        val btnDeleteRow: ImageButton = view.findViewById(R.id.btnDeleteRow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val currentWord = wordList[position]
        holder.tvWordText.text = currentWord.text

        // İlerleme yüzdesini ve çubuğunu hesapla
        val res = currentWord.progress * 16.7
        holder.tvWordPercent.text = "%" + "%.0f".format(res)
        holder.pbWordProgress.progress = (currentWord.progress * 17).toInt()

        val context = holder.itemView.context
        val wordsRepository = WordsRepository()

        if (isEditMode) {
            // Düzenleme Modu: Kalem ikonu göster ve düzenleme diyalogunu aç
            holder.btnDeleteRow.setImageResource(android.R.drawable.ic_menu_edit)
            holder.btnDeleteRow.setOnClickListener {
                val actualPosition = holder.adapterPosition
                if (actualPosition != RecyclerView.NO_POSITION) {
                    val targetWord = wordList[actualPosition]
                    selectedImageUri = null

                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Kelimeyi Düzenle")

                    val layout = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(60, 40, 60, 20)
                    }

                    // Girdi alanlarını oluştur
                    val tvEngLabel = TextView(context).apply { text = "İngilizce Kelime"; textStyleBold() }
                    val etEng = EditText(context).apply { hint = "Örn: Apple" }

                    val tvTurLabel = TextView(context).apply { text = "Türkçe Karşılığı"; textStyleBold(); setMargins(0, 20, 0, 0) }
                    val etTur = EditText(context).apply { hint = "Örn: Elma" }

                    val tvCategoryLabel = TextView(context).apply { text = "Kategori"; textStyleBold(); setMargins(0, 20, 0, 0) }
                    val etCategory = EditText(context).apply { hint = "Örn: Fruit" }

                    val tvSamplesLabel = TextView(context).apply { text = "Örnek Cümleler"; textStyleBold(); setMargins(0, 20, 0, 0) }
                    val etSamples = EditText(context).apply { hint = "Her cümleyi yeni satıra yazın" }

                    val tvImageLabel = TextView(context).apply { text = "Kelime Görseli"; textStyleBold(); setMargins(0, 20, 0, 0) }
                    val ivDialogImage = ImageView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(200, 200).apply { setMargins(0, 10, 0, 10) }
                        visibility = View.GONE
                    }
                    currentDialogImageView = ivDialogImage

                    val btnSelectImg = Button(context).apply {
                        text = "Görsel Seç / Değiştir"
                        setOnClickListener {
                            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
                            (context as? Activity)?.startActivityForResult(intent, 999)
                        }
                    }

                    // Yerleşime ekle
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

                    // Mevcut verileri çek ve alanlara yerleştir
                    wordsRepository.getWordDetails(targetWord.id) { word ->
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
                                FirebaseDatabase.getInstance("https://six-times-228d1-default-rtdb.europe-west1.firebasedatabase.app")
                                    .reference.child("Words").child(userId).child(targetWord.id).child("samples")
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

                            var startId = System.currentTimeMillis().toInt()
                            for ((index, sampleText) in samplesList.withIndex()) {
                                val sid = startId + index
                                samplesMap[sid.toString()] = mapOf("sampleID" to sid, "sample" to sampleText)
                            }

                            wordsRepository.updateWordInFirebase(
                                wordId = targetWord.id,
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
        } else {
            // Silme Modu: Çöp kutusu ikonu göster ve silme onayı iste
            holder.btnDeleteRow.setImageResource(android.R.drawable.ic_menu_delete)
            holder.btnDeleteRow.setOnClickListener {
                androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Kelimeyi Sileyim mi?")
                    .setMessage("Bu kelimeyi silmek istediğine emin misin?")
                    .setPositiveButton("Evet") { _, _ ->
                        val actualPosition = holder.adapterPosition
                        if (actualPosition != RecyclerView.NO_POSITION) {
                            val targetWord = wordList[actualPosition]
                            wordsRepository.deleteWordFromFirebase(
                                wordId = targetWord.id,
                                onSuccess = {
                                    wordList.removeAt(actualPosition)
                                    notifyItemRemoved(actualPosition)
                                    notifyItemRangeChanged(actualPosition, wordList.size)
                                    Toast.makeText(context, "Kelime kalıcı olarak silindi", Toast.LENGTH_SHORT).show()
                                },
                                onError = { errorMessage ->
                                    Toast.makeText(context, "Hata: $errorMessage", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                    .setNegativeButton("Hayır") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        }
    }

    override fun getItemCount(): Int = wordList.size

    /**
     * Seçilen görseli dahili depolamaya kopyalar.
     * 
     * @param context Uygulama bağlamı.
     * @param uri Görsel URI.
     * @return Kopyalanan dosya URI.
     */
    private fun copyImageToInternalStorage(context: android.content.Context, uri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "word_image_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(context.filesDir, fileName)
            val outputStream = java.io.FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Görsel seçim sonucunu işler.
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 999 && resultCode == Activity.RESULT_OK && data != null) {
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

    /**
     * Metin stilini kalın yapar.
     */
    private fun TextView.textStyleBold() {
        this.setTypeface(null, android.graphics.Typeface.BOLD)
        this.textSize = 14f
    }

    /**
     * Bileşene kenar boşluğu ekler.
     */
    private fun TextView.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(left, top, right, bottom)
        this.layoutParams = lp
    }
}
