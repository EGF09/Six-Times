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
import com.example.a6times.WordAdapter
import com.example.a6times.WordItem
import java.io.File
import java.io.FileOutputStream

class AnalysisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        val btnBack = findViewById<ImageButton>(R.id.btnAnalysisBack)
        btnBack.setOnClickListener { finish() }

        val btnPrint = findViewById<ImageButton>(R.id.btnPrintReport)
        btnPrint.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Raporu İndir")
            builder.setMessage("Analiz raporunuz PDF olarak kaydedilsin mi?")
            builder.setPositiveButton("İndir") { _, _ -> raporuPdfOlarakKaydet() }
            builder.setNegativeButton("İptal", null)
            builder.show()
        }

        val tvTotalWords = findViewById<TextView>(R.id.tvTotalWordsCount)
        val tvAccuracy = findViewById<TextView>(R.id.tvAccuracyRate)
        val rvHardWords = findViewById<RecyclerView>(R.id.rvHardWords)

        updateStatistics(tvTotalWords, tvAccuracy)
        setupHardWordsList(rvHardWords)
    }

    private fun updateStatistics(tvTotal: TextView, tvRate: TextView) {
        tvTotal.text = "Toplam Kelime\n45"
        tvRate.text = "Başarı Oranı\n%72"
    }

    private fun setupHardWordsList(recyclerView: RecyclerView) {
        val hardWords = listOf(WordItem("Ambiguous", 15), WordItem("Persistence", 30))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = WordAdapter(hardWords)
    }

    private fun raporuPdfOlarakKaydet() {
        val pdfDocument = PdfDocument()
        // A4 Boyutu: 595 x 842
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("6times Başarı Analiz Raporu", 50f, 80f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Rapor Detayları:", 50f, 150f, paint)
        canvas.drawText("Bu rapor otomatik olarak oluşturulmuştur.", 50f, 200f, paint)

        pdfDocument.finishPage(page)

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