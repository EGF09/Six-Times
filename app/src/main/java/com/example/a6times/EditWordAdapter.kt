package com.example.a6times.menunav

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.a6times.R
import com.example.a6times.WordItem
import com.example.a6times.data.WordsRepository

class EditWordAdapter(private val wordList: MutableList<WordItem>) : RecyclerView.Adapter<EditWordAdapter.EditViewHolder>() {

    class EditViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWordText: TextView = view.findViewById(R.id.tvWordText)
        val tvWordPercent: TextView = view.findViewById(R.id.tvWordPercent)
        val pbWordProgress: ProgressBar = view.findViewById(R.id.pbWordProgress)
        val btnDeleteRow: ImageButton = view.findViewById(R.id.btnDeleteRow) // Bu butonu KALEM olarak kullanacağız
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
        return EditViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditViewHolder, position: Int) {
        val currentWord = wordList[position]
        holder.tvWordText.text = currentWord.wordText

        val res = currentWord.progress * 16.7
        holder.tvWordPercent.text = "%" + "%.0f".format(res)
        holder.pbWordProgress.progress = (currentWord.progress * 17).toInt()

        // Çöp kutusu görselini kodla KALEM (edit) görseline dönüştürüyoruz
        holder.btnDeleteRow.setImageResource(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI.let {
            android.R.drawable.ic_menu_edit
        })

        holder.btnDeleteRow.setOnClickListener {
            val context = holder.itemView.context
            val wordsRepository = WordsRepository()

            // Mevcut kelimeyi İngilizce ve Türkçe olarak ikiye ayırıp edittext'lere dolduracağız
            val parts = currentWord.wordText.split(" - ")
            val currentEng = parts.getOrNull(0) ?: ""
            val currentTur = parts.getOrNull(1) ?: ""

            // Pop-up (Diyalog) Tasarımı oluşturuyoruz
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Kelimeyi Düzenle")

            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(50, 40, 50, 10)

            val etEng = EditText(context).apply {
                hint = "İngilizce Kelime"
                setText(currentEng)
            }
            val etTur = EditText(context).apply {
                hint = "Türkçe Karşılığı"
                setText(currentTur)
            }

            layout.addView(etEng)
            layout.addView(etTur)
            builder.setView(layout)

            builder.setPositiveButton("Güncelle") { dialog, _ ->
                val newEng = etEng.text.toString().trim()
                val newTur = etTur.text.toString().trim()

                if (newEng.isNotEmpty() && newTur.isNotEmpty()) {
                    wordsRepository.updateWordInFirebase(
                        wordId = currentWord.id,
                        newEngName = newEng,
                        newTurName = newTur,
                        onSuccess = {
                            Toast.makeText(context, "Kelime güncellendi!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, "Hata: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Alanlar boş bırakılamaz", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("İptal", null)
            builder.create().show()
        }
    }

    override fun getItemCount(): Int = wordList.size
}