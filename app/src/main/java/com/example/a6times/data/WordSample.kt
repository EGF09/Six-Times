package com.example.a6times.data

/**
 * Bir kelimeyle ilişkili örnek cümleyi temsil eden veri sınıfı.
 * 
 * @property sampleID Örnek cümlenin benzersiz kimliği.
 * @property sample Örnek cümle metni.
 */
data class WordSample (
    var sampleID : Int = 0,
    var sample : String = ""
)
