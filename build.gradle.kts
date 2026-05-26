// Tüm alt projeler/modüller için ortak olan yapılandırma seçeneklerinin eklendiği üst düzey derleme dosyası.
plugins {
    // Android uygulama eklentisini tanımlar ancak henüz uygulamaz.
    alias(libs.plugins.android.application) apply false
    // Google hizmetleri eklentisini tanımlar ancak henüz uygulamaz (Firebase vb. için).
    alias(libs.plugins.google.gms.google.services) apply false
}