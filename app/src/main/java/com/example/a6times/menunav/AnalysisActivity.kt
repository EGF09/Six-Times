package com.example.a6times.menunav

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a6times.R
import com.example.a6times.adapters.TopicProgressAdapter
import com.example.a6times.adapters.WordAdapter
import com.example.a6times.data.WordItem
import java.io.File
import java.io.FileOutputStream

/**
 * Kullanıcının kelime öğrenme istatistiklerini analiz eden ve raporlayan ekran.
 * Kategori bazlı ilerleme gösterimi ve PDF raporu oluşturma özelliklerini içerir.
 */
class AnalysisActivity : AppCompatActivity() {

    private var currentTotalWords: Int = 0
    private var currentOverallAccuracy: Int = 0
    private var currentProgressList: List<com.example.a6times.data.TopicProgress> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        val btnBack = findViewById<ImageButton>(R.id.WordBackButton)
        btnBack.setOnClickListener { finish() }

        val btnPrint = findViewById<ImageButton>(R.id.btnPrintReport)
        btnPrint.setOnClickListener {
            // PDF raporu indirme onayı
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Raporu İndir")
            builder.setMessage("Analiz raporunuz PDF olarak kaydedilsin mi?")
            builder.setPositiveButton("İndir") { _, _ -> raporuPdfOlarakKaydet() }
            builder.setNegativeButton("İptal", null)
            builder.show()
        }

        val tvTotalWords = findViewById<TextView>(R.id.tvTotalWordsCount)
        val tvAccuracy = findViewById<TextView>(R.id.tvAccuracyRate)
        val rvTopicProgress = findViewById<RecyclerView>(R.id.rvTopicProgress)
        rvTopicProgress.layoutManager = LinearLayoutManager(this)

        // Verileri Firebase'den dinle ve istatistikleri güncelle
        val wordsRepository = com.example.a6times.data.WordsRepository()
        wordsRepository.listenToWords(
            onDataChange = { wordsList ->
                updateStatisticsAndProgress(wordsList, tvTotalWords, tvAccuracy, rvTopicProgress)
            },
            onError = { error ->
                Toast.makeText(this, "Veriler alınamadı: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Kelime listesine göre genel istatistikleri ve kategori bazlı ilerlemeyi günceller.
     */
    private fun updateStatisticsAndProgress(
        words: List<com.example.a6times.data.Words>,
        tvTotal: TextView,
        tvRate: TextView,
        rvTopicProgress: RecyclerView
    ) {
        currentTotalWords = words.size
        // İlerleme kaydedilmiş (progress > 0) kelimeleri doğru bilinmiş/öğrenilmeye başlanmış kabul et
        val correctWords = words.count { it.progress > 0 }

        currentOverallAccuracy = if (currentTotalWords > 0) {
            ((correctWords.toDouble() / currentTotalWords.toDouble()) * 100).toInt()
        } else {
            0
        }

        tvTotal.text = "Toplam Kelime\n$currentTotalWords"
        tvRate.text = "Başarı Oranı\n%$currentOverallAccuracy"

        // Kategori bazlı ilerlemeyi hesapla ve listele
        currentProgressList = calculateTopicProgress(words)
        rvTopicProgress.adapter = TopicProgressAdapter(currentProgressList)
    }

    /**
     * Kelimeleri kategorilerine göre gruplayarak her kategorinin başarı yüzdesini hesaplar.
     */
    private fun calculateTopicProgress(words: List<com.example.a6times.data.Words>): List<com.example.a6times.data.TopicProgress> {
        val groupedByCategory = words.groupBy { it.category }
        val progressList = mutableListOf<com.example.a6times.data.TopicProgress>()

        for ((category, categoryWords) in groupedByCategory) {
            val totalCount = categoryWords.size
            val correctCount = categoryWords.count { it.progress > 0 }

            val percentage = if (totalCount > 0) {
                ((correctCount.toDouble() / totalCount.toDouble()) * 100).toInt()
            } else {
                0
            }

            val topicName = if (category.isNotEmpty()) category else "Diğer"
            progressList.add(
                com.example.a6times.data.TopicProgress(
                    topic = topicName,
                    correctCount = correctCount,
                    totalCount = totalCount,
                    progressPercentage = percentage
                )
            )
        }

        return progressList.sortedByDescending { it.progressPercentage }
    }

    /**
     * Analiz sonuçlarını içeren bir PDF dokümanı oluşturur ve cihazın dahili depolamasına kaydeder.
     */
    private fun raporuPdfOlarakKaydet() {
        val pdfDocument = PdfDocument()
        // A4 Boyutu: 595 x 842 piksel
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Başlık
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("6times Başarı Analiz Raporu", 50f, 80f, paint)

        // Genel Bilgiler
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Genel İstatistikler", 50f, 130f, paint)

        paint.isFakeBoldText = false
        canvas.drawText("Toplam Öğrenilen Kelime: $currentTotalWords", 50f, 160f, paint)
        canvas.drawText("Genel Başarı Oranı: %$currentOverallAccuracy", 50f, 185f, paint)

        // Kategori Detayları
        paint.isFakeBoldText = true
        canvas.drawText("Kategori Bazlı İlerleme", 50f, 235f, paint)

        paint.isFakeBoldText = false
        var currentY = 265f
        for (progress in currentProgressList) {
            val progressText = "${progress.topic}: ${progress.correctCount}/${progress.totalCount} (%${progress.progressPercentage})"
            canvas.drawText(progressText, 50f, currentY, paint)
            currentY += 25f
        }

        // Alt Bilgi
        currentY += 30f
        paint.textSize = 10f
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("Bu rapor otomatik olarak oluşturulmuştur.", 50f, currentY, paint)

        pdfDocument.finishPage(page)

        // Dosyayı oluştur ve kaydet
        val dosyaAdi = "6times_Analiz_${System.currentTimeMillis()}.pdf"
        val dosyaYolu = File(getExternalFilesDir(null), dosyaAdi)

        try {
            pdfDocument.writeTo(FileOutputStream(dosyaYolu))
            Toast.makeText(this, "Rapor indirildi. Açılıyor...", Toast.LENGTH_SHORT).show()
            openPDF(dosyaYolu)
        } catch (e: Exception) {
            Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }

    /**
     * Oluşturulan PDF dosyasını uygun bir uygulama ile açar.
     */
    private fun openPDF(file: File) {
        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "PDF açacak uygulama bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }
}
