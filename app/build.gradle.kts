import java.util.Properties
import java.io.FileInputStream

// Uygulama seviyesindeki eklentiler
plugins {
    alias(libs.plugins.android.application) // Android uygulama yapılandırması
    alias(libs.plugins.google.gms.google.services) // Google Hizmetleri (Firebase vb.)
}

android {
    namespace = "com.example.a6times"
    compileSdk = 36 // Uygulamanın derlendiği Android SDK sürümü

    defaultConfig {
        applicationId = "com.example.a6times" // Uygulamanın benzersiz kimliği
        minSdk = 24 // Desteklenen en düşük Android sürümü (Nougat)
        targetSdk = 34 // Hedeflenen Android SDK sürümü
        versionCode = 1 // Uygulama sürüm kodu (Market güncellemeleri için)
        versionName = "1.0" // Kullanıcıya gösterilen sürüm adı

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Modern Android geliştirme araçlarının etkinleştirilmesi
    buildFeatures {
        viewBinding = true // XML görünümlerine güvenli erişim
        dataBinding = true // Veri ve görünümleri doğrudan bağlama
        buildConfig = true // Kod içinden derleme bilgilerine erişim
    }

    // Derleme türleri yapılandırması
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false // Kod küçültme (obfuscation) kapalı
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java uyumluluk ayarları
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Kotlin derleyici ayarları
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

// Proje bağımlılıkları (Kütüphaneler)
dependencies {
    // Tasarım ve Arayüz Bileşenleri
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Veritabanı ve Arka Plan Hizmetleri (Firebase)
    implementation(libs.firebase.database)
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Yaşam Döngüsü ve ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")

    // Görsel Efektler ve Resim Yükleme
    implementation("nl.dionsegijn:konfetti-xml:2.0.4") // Kutlama efekti
    implementation("io.coil-kt:coil:2.6.0") // Resim yükleme kütüphanesi

    // Test Bağımlılıkları
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}