package com.example.a6times.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a6times.R
import com.example.a6times.data.TopicProgress

/**
 * Konu bazlı öğrenme ilerlemesini listeleyen RecyclerView adaptörü.
 * 
 * @property progressList Gösterilecek konu ilerleme verilerinin listesi.
 */
class TopicProgressAdapter(private val progressList: List<TopicProgress>) :
    RecyclerView.Adapter<TopicProgressAdapter.ProgressViewHolder>() {

    /**
     * Her bir konu ilerleme öğesinin görünüm bileşenlerini tutan ViewHolder sınıfı.
     */
    class ProgressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTopicName: TextView = view.findViewById(R.id.tvTopicName)
        val tvProgressText: TextView = view.findViewById(R.id.tvProgressText)
        val progressBarTopic: ProgressBar = view.findViewById(R.id.progressBarTopic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic_progress, parent, false)
        return ProgressViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val item = progressList[position]
        
        // Verileri görünüm bileşenlerine bağla
        holder.tvTopicName.text = item.topic
        holder.tvProgressText.text = "${item.correctCount}/${item.totalCount} (%${item.progressPercentage})"
        
        // İlerleme çubuğunu güncelle
        holder.progressBarTopic.progress = item.progressPercentage
    }

    override fun getItemCount(): Int = progressList.size
}
