package com.example.a6times.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a6times.R
import com.example.a6times.data.WordItem

/**
 * Ana kelime listesi için RecyclerView adaptörü.
 * Moduna göre (Düzenleme veya Silme) farklı işlevler sunar.
 * 
 * @property wordList Görüntülenecek kelime öğelerinin listesi.
 * @property onEditClick Kelimeyi düzenlemek için tıklanma olayı.
 * @property onDeleteClick Kelimeyi silmek için tıklanma olayı.
 */
class WordAdapter(
    private val wordList: MutableList<WordItem>,
    private val onEditClick: (WordItem) -> Unit,
    private val onDeleteClick: (WordItem) -> Unit
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    /** Düzenleme modu aktif mi? (true: düzenleme, false: silme) */
    var isEditMode: Boolean = false

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
        val res = (currentWord.progress.toDouble() * 100) / com.example.a6times.utils.Constants.MAX_WORD_PROGRESS
        holder.tvWordPercent.text = "%${res.toInt()}"
        holder.pbWordProgress.progress = res.toInt()

        if (isEditMode) {
            // Düzenleme Modu: Kalem ikonu göster
            holder.btnDeleteRow.setImageResource(android.R.drawable.ic_menu_edit)
            holder.btnDeleteRow.setOnClickListener {
                val actualPosition = holder.adapterPosition
                if (actualPosition != RecyclerView.NO_POSITION) {
                    onEditClick(wordList[actualPosition])
                }
            }
        } else {
            // Silme Modu: Çöp kutusu ikonu göster
            holder.btnDeleteRow.setImageResource(android.R.drawable.ic_menu_delete)
            holder.btnDeleteRow.setOnClickListener {
                val actualPosition = holder.adapterPosition
                if (actualPosition != RecyclerView.NO_POSITION) {
                    onDeleteClick(wordList[actualPosition])
                }
            }
        }
    }

    override fun getItemCount(): Int = wordList.size
}
