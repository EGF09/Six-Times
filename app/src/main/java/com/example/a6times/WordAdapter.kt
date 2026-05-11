package com.example.a6times

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WordAdapter(private val wordList: List<WordItem>) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWordText: TextView = view.findViewById(R.id.tvWordText)

        // Kırmızı hatayı çözen satır:
        val tvWordPercent: TextView = view.findViewById(R.id.tvWordPercent)

        val pbWordProgress: ProgressBar = view.findViewById(R.id.pbWordProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val currentWord = wordList[position]

        holder.tvWordText.text = currentWord.wordText

        // Artık burası kırmızı yanmayacak:
        holder.tvWordPercent.text = "%${currentWord.progress}"

        holder.pbWordProgress.progress = currentWord.progress
    }

    override fun getItemCount(): Int {
        return wordList.size
    }
}