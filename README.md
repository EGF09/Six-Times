# 🧠 Six-Times - Language Learning & Vocabulary App

Six-Times, aralıklı tekrar (spaced-repetition) ve oyunlaştırma yöntemlerini kullanarak kullanıcıların kalıcı olarak yeni kelimeler öğrenmesini sağlayan, Android platformu için geliştirilmiş modern bir mobil uygulamadır.

İsminden de anlaşılacağı üzere temel felsefesi: *Bir kelimeyi tam anlamıyla öğrenmek için onunla farklı bağlamlarda (okuma, dinleme, test, oyun) yeterince sık (örn. altı kez) karşılaşmak.*

## 📸 Ekran Görüntüleri
<table table-layout="fixed" width="100%">
  <tr>
    <td>
      <img src="https://github.com/user-attachments/assets/91dd8131-e7e4-4513-8926-a24aa114d986" width="100%" alt="img2" />
    </td>
    <td>
      <img src="https://github.com/user-attachments/assets/16d67cec-a1d1-4280-ab68-9d2517c71aff" width="100%" alt="img1" />
    </td>
  </tr>
</table>


## 🌟 Öne Çıkan Özellikler

*   **🔒 Kullanıcı Yönetimi (Firebase Auth):** Güvenli kayıt olma, giriş yapma ve şifre sıfırlama (şifre onaylama) işlemleri.
*   **📚 Kelime Havuzu ve Yönetimi:** Kullanıcıların kendi kelimelerini, anlamlarını ve örnek cümlelerini ekleyip düzenleyebilmeleri.
*   **🎮 Oyunlaştırma (Wordle Modu):** Öğrenilen kelimelerin akılda kalıcılığını artırmak için popüler kelime bulmaca oyunu Wordle konseptinin entegrasyonu.
*   **📖 Hikaye Modu (Contextual Learning):** Kelimeleri sadece tekil olarak değil, hikayeler (Story Detail Activity) içerisinde görerek bağlamsal öğrenme imkanı.
*   **📝 Sınav ve Analiz (Exam & Analysis):** Kullanıcının gelişimini ölçmesi için sınav modülü, başarı durumlarını grafiksel / istatistiksel analiz sayfalarında görebilme.
*   **🎨 Neon UI Tasarımı:** Kullanıcı deneyimini artıran, özel hazırlanmış şık Neon butonlar, kartlar ve arka planlar (`bg_neon_card`, `bg_neon_button`).
*   **🔊 Sesli Geri Bildirimler:** Doğru cevaplarda ve sınavlarda sesli etkileşimler (`correct_sound.mp3`, `exam_fail.mp3`).

## 🛠 Kullanılan Teknolojiler ve Mimari

*   **Platform:** Android (Min SDK desteği Gradle yapılandırmalarına göre belirlenmiştir)
*   **Dil:** Kotlin 
*   **Mimari & Veri Yönetimi:** Repository Pattern (`IUsersRepository`, `UsersRepository`, `WordsRepository`) ile temiz kod mimarisi.
*   **Backend & Veritabanı:** Firebase (Kullanıcı verileri ve kelime ilerlemeleri için `google-services.json` mevcut).
*   **Kullanıcı Arayüzü (UI):** Özel XML tasarımları, animasyonlar (card flip), ilerleme çubukları (`TopicProgressAdapter`).
*   **Build Sistemi:** Gradle (Kotlin DSL ile `build.gradle.kts` kullanılarak).

## 📂 Proje Yapısı

\`\`\`text
app/src/main/java/com/example/a6times/
├── data/           # Veri modelleri, interface'ler ve Repository sınıfları (Users, Words, TopicProgress vb.)
├── loginnav/       # Kimlik doğrulama akışı (Login, Register, Forgot Password)
├── menunav/        # Uygulama ana menüleri (Home, AddWord, Settings, Analysis)
└── ...             # Adapter sınıfları, ExamActivity, WordleActivity, StoryDetailActivity
\`\`\`

## 🚀 Kurulum ve Çalıştırma

Projeyi yerel makinenizde çalıştırmak için aşağıdaki adımları izleyin:

1.  **Depoyu Klonlayın:**
    \`\`\`bash
    git clone https://github.com/your-username/Six-Times.git
    \`\`\`
2.  **Android Studio'da Açın:**
    *   Android Studio'yu başlatın ve *Open an existing Android Studio project* (Mevcut bir projeyi aç) seçeneğine tıklayarak klonladığınız dizini seçin.
3.  **Firebase Kurulumu:**
    *   Projenin veritabanı ve Auth işlemleri için kendi Firebase projenizi oluşturun.
    *   Firebase konsolundan indirdiğiniz \`google-services.json\` dosyasını \`app/ \` dizinine (mevcut dosyanın üzerine) yapıştırın.
4.  **Projeyi Senkronize Edin ve Çalıştırın:**
    *   Gradle senkronizasyonunun bitmesini bekleyin.
    *   Bir emülatör veya fiziksel cihaz seçerek **Run (Çalıştır)** butonuna (Shift + F10) basın.

---
*Developed with ❤️ for better learning.*
